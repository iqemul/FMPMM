package edu.hit.fmpmm.service.process;

import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import edu.hit.fmpmm.domain.exception.ExecutionLogicException;
import edu.hit.fmpmm.domain.neo4j.node.*;
import edu.hit.fmpmm.domain.sim.Dummy;
import edu.hit.fmpmm.domain.sim.SimClient;
import edu.hit.fmpmm.domain.sim.SimObject;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.dto.Result;
import edu.hit.fmpmm.repo.InstanceRepository;
import edu.hit.fmpmm.service.aapc.aa.ActionActuator;
import edu.hit.fmpmm.service.aapc.aa.ActionFactory;
import edu.hit.fmpmm.service.aapc.pc.ObjectParameterCalculator;
import edu.hit.fmpmm.service.aapc.pc.ParameterCalculator;
import edu.hit.fmpmm.service.aapc.pc.ParameterFactory;
import edu.hit.fmpmm.service.aapc.pc.RobotParameterCalculator;
import edu.hit.fmpmm.service.aapc.pc.parameters.Accel;
import edu.hit.fmpmm.service.aapc.pc.parameters.Jerk;
import edu.hit.fmpmm.service.aapc.pc.parameters.Pose;
import edu.hit.fmpmm.service.aapc.pc.parameters.Velocity;
import edu.hit.fmpmm.service.sim.SimClientFactory;
import edu.hit.fmpmm.util.DataProcessor;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.StringValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessService {
    private final DataProcessor dataProcessor = new DataProcessor();  // 一个Util，处理数据
    private final ParameterFactory parameterFactory;  // 生产参数计算器
    private final SimClientFactory simClientFactory;  // 生产CoppeliaSim的客户端和主要工具
    private final ActionFactory actionFactory;  // 生产动作执行器
    private final ProcessUtil processUtil;
    private SimClient simClient;
    private final InstanceRepository instanceRepo;

    @Autowired
    public ProcessService(
            ParameterFactory parameterFactory, SimClientFactory simClientFactory, ActionFactory actionFactory, ProcessUtil processUtil, InstanceRepository instanceRepo) {
        // 操作Abstraction Node
        this.parameterFactory = parameterFactory;
        this.simClientFactory = simClientFactory;
        this.actionFactory = actionFactory;
        this.processUtil = processUtil;
        this.instanceRepo = instanceRepo;
    }

    public Result prepare(List<Instance> objs) {  // 将料仓上的零件放置在试验台上
        // 对于两个物料，检查每一个是否在实验台上，如果不在，让机器人抓到实验台上
        List<String> robotCodes = new ArrayList<>();  // TODO 在哪里定义
        robotCodes.add("AUR0001");
        robotCodes.add("AUR0002");
        List<String> gripperCodes = new ArrayList<>();
        gripperCodes.add("GDH0001");
        gripperCodes.add("GDH0002");
        // 创建机器人对象
        List<Robot> robots = new ArrayList<>();
        for (int i = 0; i < robotCodes.size(); i++) {
            String robotCode = robotCodes.get(i);  // 根据编号从知识图谱中找一个机械臂Instance
            Instance armInstance = instanceRepo.findInstanceByCode(robotCode);
            if (armInstance == null) {
                throw new ExecutionLogicException("找不到 robot " + robotCode);
            }
            String gripperCode = gripperCodes.get(i);
            Instance gripperInstance = instanceRepo.findInstanceByCode(gripperCode);
            if (gripperInstance == null) {
                throw new ExecutionLogicException("找不到 gripper " + gripperCode);
            }
            actionFactory.setRobotNode(armInstance);
            actionFactory.setGripperNode(gripperInstance);
            actionFactory.setSimClient(simClient);
            Robot robot = actionFactory.getSimRobot();
            robots.add(robot);
        }
        try {
            SimObject workstation = new SimObject(simClient, "/workstation");
            // 实验台的外形包围盒
            RemoteAPIObjects._sim workstationSim = workstation.getClient().getSim();
            List<Double> workstationShapeBB = workstationSim.getShapeBB(workstation.getHandle());  // xyz
            // 实验台的位置
            List<Double> workstationPosition = workstation.getPosition();
            Set<Instance> objSet = new HashSet<>(objs);
            while (!objSet.isEmpty()) {
                for (Instance obj : objSet) {
                    SimObject simObject = getSimObject(obj);
                    List<Double> objectPosition = simObject.getPosition();
                    // 判断obj位置是否在实验台上
                    if (objectPosition.get(0) >= workstationPosition.get(0) - workstationShapeBB.get(0) / 2 &&
                            objectPosition.get(0) <= workstationPosition.get(0) + workstationShapeBB.get(0) / 2 &&
                            objectPosition.get(1) >= workstationPosition.get(1) - workstationShapeBB.get(1) / 2 &&
                            objectPosition.get(1) <= workstationPosition.get(1) + workstationShapeBB.get(1) / 2) {
                        objSet.remove(obj);
                        break;
                    }
                    // 不在，让机器人抓到实验台上 离哪个机器人近
                    Robot pickUpRobot = getPickUpRobot(robots, objectPosition);
                    // 机器人开抓 抓到哪里？
                    Dummy dest = getDestDummy(pickUpRobot);
                    pickUp(pickUpRobot, obj, dest.getPose());
                }
            }
        } catch (CborException e) {
            System.out.println("出问题了：" + e.getMessage());
            try {
                stopSim();
            } catch (CborException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
        return Result.success();
    }

    private SimObject getSimObject(Instance obj) {
        Map<String, Object> objOtherProperties = obj.getOtherProperties();
        String objCode = ((StringValue) objOtherProperties.getOrDefault("code", "")).asString();
        if ("".equals(objCode)) {
            throw new ExecutionLogicException("知识图谱中找不到 " + obj.getName() + " 的code属性");
        }
        return new SimObject(simClient, "/" + objCode);
    }

    private Dummy getDestDummy(Robot pickUpRobot) {
        Dummy pos1 = new Dummy(simClient, "/pos1");
        Dummy pos2 = new Dummy(simClient, "/pos2");
        double minDistance = Double.MAX_VALUE;
        Dummy dest = pos1;
        for (Dummy pos : new Dummy[]{pos1, pos2}) {
            List<Double> dummyPosition = pos.getPosition();
            double distance = Math.sqrt(
                    Math.pow(dummyPosition.get(0) - pickUpRobot.getPosition().get(0), 2) +
                            Math.pow(dummyPosition.get(1) - pickUpRobot.getPosition().get(1), 2)
            );
            if (distance < minDistance) {
                minDistance = distance;
                dest = pos;
            }
        }
        return dest;
    }

    private static Robot getPickUpRobot(List<Robot> robots, List<Double> objectPosition) {
        Robot pickUpRobot = robots.get(0);
        double minDistance = Double.MAX_VALUE;
        for (Robot robot : robots) {
            List<Double> robotPosition = robot.getPosition();
            double distance = Math.sqrt(
                    Math.pow(objectPosition.get(0) - robotPosition.get(0), 2) +
                            Math.pow(objectPosition.get(1) - robotPosition.get(1), 2)
            );
            if (distance < minDistance) {
                minDistance = distance;
                pickUpRobot = robot;
            }
        }
        return pickUpRobot;
    }

    private void pickUp(Robot robot, Instance object, List<Double> des) {  // 使用某个机器人抓取某个零件放到某个位置
        try {
            // 设置相关参数
            RobotParameterCalculator calculator = new Velocity();
            @SuppressWarnings("unchecked")
            ArrayList<Double> vel = (ArrayList<Double>) calculator.value();
            robot.setMaxVel(vel);
            calculator = new Accel();
            @SuppressWarnings("unchecked")
            ArrayList<Double> accel = (ArrayList<Double>) calculator.value();
            robot.setMaxAccel(accel);
            calculator = new Jerk();
            @SuppressWarnings("unchecked")
            ArrayList<Double> jerk = (ArrayList<Double>) calculator.value();
            robot.setMaxJerk(jerk);
            // =1= 张开夹爪
            robot.openGripper();
            // 获取物体位置 上表面
            SimObject simObject = getSimObject(object);
            List<Double> objectPosition = simObject.getPosition();
            List<Instance> otherFeatureCaps = processUtil.findFeatureByInstanceAndType(object, "其他特征");
            ObjectParameterCalculator poseCal = new Pose();
            Instance boxSize = poseCal.getBoxSize(otherFeatureCaps);
            Map<String, Object> boxSizeOtherProperties = boxSize.getOtherProperties();
            double height = ((IntegerValue) boxSizeOtherProperties.getOrDefault("高度", 0)).asDouble() / 1000.0;
            objectPosition.set(2, objectPosition.get(2) + height / 2);
            // 要抓的位置 减去抓取特征的高度
            List<Instance> grabFeaturesCaps = processUtil.findFeatureByInstanceAndType(object, "抓取特征");
            if (grabFeaturesCaps.isEmpty()) {
                throw new ExecutionLogicException("检索不到 " + object.getName() + " 的抓取特征能力");
            }
            Instance grabFeatureCap = grabFeaturesCaps.get(0);
            Map<String, Object> grabFeatureCapOtherProperties = grabFeatureCap.getOtherProperties();
            double grabHeight = ((IntegerValue) grabFeatureCapOtherProperties.getOrDefault("高度", 0)).asDouble() / 1000.0;
            objectPosition.set(2, objectPosition.get(2) - grabHeight / 2 - 0.009);
            List<Double> gotoPose = robot.getTip().getPose();
            for (int i = 0; i < 3; i++) {
                gotoPose.set(i, objectPosition.get(i));
            }
            // =2= 去抓
            robot.moveByEnd(gotoPose);
            // =3= 闭合
            robot.closeGripper();
            // =4= 提起来一点
            gotoPose.set(2, gotoPose.get(2) + poseCal.getSAFE_DISTANCE_LIFT());
            robot.moveByEnd(gotoPose);
            // 目标位置 = des + 物体高度 - 抓取高度
            des.set(2, des.get(2) + height - grabHeight + poseCal.getSAFE_DISTANCE());
            // =5= 去目标位置
            robot.moveByEnd(des);
            // =6= 松手
            robot.openGripper();
            // =7= 回去
            robot.moveByEnd(robot.getGoal().getPose());
        } catch (CborException e) {
            System.out.println("出问题了：" + e.getMessage());
            try {
                stopSim();
            } catch (CborException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理工艺列表
     *
     * @param processes 工艺列表
     * @return 这个场景的工艺执行是否动作执行正反馈均为true
     */
    public Result handleProcess(List<Map<String, List<Node>>> processes) {
        try {
            startSim();  // 设置仿真客户端(假设实验都是串行进行的，所以机器人都用同一个客户端)
            for (Map<String, List<Node>> process : processes) {  // 处理每个工艺
                List<Instance> objs = new ArrayList<>();
                for (Node n : process.get("objs")) {
                    objs.add((Instance) n);
                }
                Goal processGoal = (Goal) process.get("processGoal").get(0);
                Instance processInstance = (Instance) process.get("process").get(0);

                Result isReady = prepare(objs);  // 准备物料 20240708
                if (isReady == null || !isReady.getSuccess()) {
                    return isReady;
                }
                Result result = analyze(objs, processGoal, processInstance);
                if (!result.getSuccess()) {
                    return result;
                }
            }
            stopSim();
        } catch (CborException | ExecutionLogicException e) {
            // throw new RuntimeException(e);
            System.out.println("出问题了：" + e.getMessage());
            return Result.fail(e.getMessage());
        }
        return Result.success(processes);
    }

    /**
     * 一个工艺的分析
     *
     * @param objs    这个工艺的操作对象Nodes
     * @param goal    表示这个工艺的Node
     * @param process 要执行的工艺实例
     *                //     * @param robots 这个工艺使用的机器人Nodes
     */
    public Result analyze(List<Instance> objs, Goal goal, Instance process) throws CborException {  // , List<Instance> robots
        int actionSum = 0;  // 动作执行总个数
        int actionSuccess = 0;  // 动作执行有正反馈的个数
        try {
            // 设置操作对象、机器人在这个工艺中饰演的角色
            Map<String, Instance> roles = setRoles(process, goal, objs);
            // 设置其他资源：其他能力
            Map<String, List<Instance>> otherCapabilities = getOtherCapabilities(roles);
            List<Operation> operationsTmp = goal.getOperations();
            @SuppressWarnings("unchecked")
            List<Operation> operations = (List<Operation>) dataProcessor.toPoSort(operationsTmp);
            for (Operation operation : operations) {  // 工步
                System.out.println(operation.getName());  //
                // 该工步下的操作对象和正在使用的机器人
                Map<String, Object> resource_flag = getResource(operation, roles);
                @SuppressWarnings("unchecked")
                Map<String, Instance> resource = (Map<String, Instance>) resource_flag.get("resource");
                // 判断此Operation是否需要执行
                if (!(boolean) resource_flag.get("isExecute")) {
                    continue;
                }
                // 给工厂资源
                actionFactory.setRobotNode(resource.get("operateArm"));  // 根据Node创建Robot类子类的对象
                actionFactory.setGripperNode(resource.get("operateGripper"));
                actionFactory.setSimClient(simClient);
                Robot robot = actionFactory.getSimRobot();
                parameterFactory.setRobot(robot);
                parameterFactory.setResource(resource);
                parameterFactory.setOtherCapabilities(otherCapabilities);
                parameterFactory.setSimClient(simClient);
                parameterFactory.setOperation(operation);
                List<Action> actionsTmp = operation.getHasActions();
                @SuppressWarnings("unchecked")
                List<Action> actions = (List<Action>) dataProcessor.toPoSort(actionsTmp);
                for (Action action : actions) {  // 动作
                    String actionName = action.getName();
                    System.out.println("    " + actionName);  //
                    List<Parameter> parameters = action.getHasParameters();
                    Map<String, Object> params = new HashMap<>();
                    parameterFactory.setAction(action);
                    for (Parameter parameter : parameters) {  // 参数
                        String parameterName = parameter.getName();
                        // 获得参数计算器来计算参数
                        ParameterCalculator parameterCalculator = parameterFactory.createParameter(parameterName);
                        Object value = parameterCalculator.value();
                        params.put(parameterName, value);
                        System.out.println("        " + parameterName + "=" + value);
                    }
                    ActionActuator actionActuator = actionFactory.createAction(actionName);
                    boolean flag = actionActuator.go(params);  // 执行动作
                    actionSum++;
                    if (flag) {
                        actionSuccess++;
                    }
                    System.out.println("    flag=" + flag);  //
                }
            }
            System.out.println("动作正反馈个数:动作总个数=" + actionSuccess + ":" + actionSum);
        } catch (CborException | ExecutionLogicException e) {
            // throw new ExecutionLogicException("动作执行遇到困难");
            return Result.fail(e.getMessage());
        }
//        return actionSum == actionSuccess;
        return Result.success();
    }

    public void loadSceneFile(String path) throws CborException {  // 设置场景文件
        simClientFactory.setScene(path);
        SimClient sc = simClientFactory.createSimClient();
        this.setSimClient(sc);
        simClient.getSim().loadScene(simClient.getScene());
    }

    public void startSim() throws CborException {
        if (simClient == null) {
            SimClient sc = simClientFactory.createSimClient();
            this.setSimClient(sc);
        }
        RemoteAPIObjects._sim sim = simClient.getSim();
        sim.startSimulation();
    }

    public void stopSim() throws CborException {
        simClient.getSim().stopSimulation();
    }

    private Map<String, Instance> setRoles(Instance process, Goal goal, List<Instance> objs) {  // , List<Instance> robots
        Map<String, Instance> roles = new HashMap<>();
        Map<String, Object> otherProperties = process.getOtherProperties();
        String baseFeature = ((StringValue) otherProperties.get("base_feature")).asString();
        String noBaseFeature = ((StringValue) otherProperties.get("no_base_feature")).asString();
        // 找对象的装配特征能力
        List<Instance> res = processUtil.findFeatureByInstanceAndType(objs.get(0), "装配特征");
        if (res == null) {
            throw new ExecutionLogicException("检索不到装配特征能力");
        }
        Instance assFeature1 = res.get(0);
        res = processUtil.findFeatureByInstanceAndType(objs.get(1), "装配特征");
        if (res == null) {
            throw new ExecutionLogicException("检索不到装配特征能力");
        }
        Instance assFeature2 = res.get(0);
        if (assFeature1 == null || assFeature2 == null) {
            throw new ExecutionLogicException("匹配不到对象的装配特征能力");
        }
        if (baseFeature.equals(assFeature1.getName()) && noBaseFeature.equals(assFeature2.getName())) {
            roles.put("baseAssemFeature", assFeature1);
            roles.put("noBaseAssemFeature", assFeature2);
            roles.put("base", objs.get(0));
            roles.put("noBase", objs.get(1));

        } else if (baseFeature.equals(assFeature2.getName()) && noBaseFeature.equals(assFeature1.getName())) {
            roles.put("baseAssemFeature", assFeature2);
            roles.put("noBaseAssemFeature", assFeature1);
            roles.put("base", objs.get(1));
            roles.put("noBase", objs.get(0));

        } else {
            throw new ExecutionLogicException("无法匹配操作对象和工艺目标");
        }
        // 找抓取目标
        Goal grabGoal4base = null;
        Goal grabGoal4noBase = null;
        List<Goal> goals4base = roles.get("base").getAchieveGoals();
        for (Goal g : goals4base) {
            if (!Objects.equals(g.getId(), goal.getId())) {  // !g.equals(goal)
                grabGoal4base = g;
                break;
            }
        }
        List<Goal> goals4noBase = roles.get("noBase").getAchieveGoals();
        for (Goal g : goals4noBase) {
            if (!Objects.equals(g.getId(), goal.getId())) {  // !g.equals(goal)
                grabGoal4noBase = g;
                break;
            }
        }
        // 找抓取能力 这里匹配到的抓取能力使用的机器人要和指定的机器人匹配
        if (grabGoal4base != null) {  // 若基准对象有抓取目标
            res = processUtil.findFeatureByInstanceAndType(roles.get("base"), "抓取特征");
            if (res == null) {
                throw new ExecutionLogicException("检索不到抓取特征能力");
            }
            Instance baseGrabFeature = res.get(0);  // 假设一个对象有一个抓取特征
            if (baseGrabFeature != null) {
                roles.put("baseGrabFeature", baseGrabFeature);
                // 机器人
                Instance gripper = processUtil.findGripperByGoal(grabGoal4base);
                if (gripper == null) {
                    throw new ExecutionLogicException("无法匹配到夹爪");
                }
//                Instance robot = findRobotFromRobotsByGriper(gripper, robots);
                Instance robot = findRobotByGriper(gripper);
                if (robot == null) {
                    throw new ExecutionLogicException("无法匹配到机械臂");
                }
                roles.put("baseGripper", gripper);
                roles.put("baseArm", robot);
            } else {
                throw new ExecutionLogicException("无法检索到抓取特征能力");
            }
        } else {
            roles.put("baseGrabFeature", null);
            roles.put("baseGripper", null);
            roles.put("baseArm", null);
        }
        if (grabGoal4noBase != null) {
            res = processUtil.findFeatureByInstanceAndType(roles.get("noBase"), "抓取特征");
            if (res == null) {
                throw new ExecutionLogicException("检索不到抓取特征能力");
            }
            Instance noBaseGrabFeature = res.get(0);
            if (noBaseGrabFeature != null) {
                roles.put("noBaseGrabFeature", noBaseGrabFeature);
                Instance gripper = processUtil.findGripperByGoal(grabGoal4noBase);
                if (gripper == null) {
                    throw new ExecutionLogicException("无法匹配到夹爪");
                }
//                Instance robot = findRobotFromRobotsByGriper(gripper, robots);
                Instance robot = findRobotByGriper(gripper);
                if (robot == null) {
                    throw new ExecutionLogicException("无法匹配到机械臂");
                }
                roles.put("noBaseGripper", gripper);
                roles.put("noBaseArm", robot);
            } else {
                throw new ExecutionLogicException("无法检索到抓取特征能力");
            }
        } else {
            roles.put("noBaseGrabFeature", null);
            roles.put("noBaseGripper", null);
            roles.put("noBaseArm", null);
        }
        return roles;
    }

    private void setSimClient(SimClient simClient) {
        this.simClient = simClient;
    }

    private Map<String, List<Instance>> getOtherCapabilities(Map<String, Instance> roles) {
        // 其他特征能力
        List<Instance> baseOtherFeatures = processUtil.findFeatureByInstanceAndType(roles.get("base"), "其他特征");
        List<Instance> noBaseOtherFeatures = processUtil.findFeatureByInstanceAndType(roles.get("noBase"), "其他特征");
        Map<String, List<Instance>> capabilities = new HashMap<>();
        capabilities.put("baseOtherFeatures", baseOtherFeatures);
        capabilities.put("noBaseOtherFeatures", noBaseOtherFeatures);
        return capabilities;
    }

    private Instance findRobotByGriper(Instance gripper) {
        List<Instance> installedOnRobots = gripper.getInstalledOns();
        if (installedOnRobots.isEmpty()) return null;
        return installedOnRobots.get(0);  // TODO
    }

    private Map<String, Object> getResource(Operation operation, Map<String, Instance> roles) {
//        System.out.println("operation:"+operation);
        Map<String, Instance> resource = new HashMap<>();
        String objType = operation.getObj();
        if (objType == null || objType.isEmpty()) {
            throw new ExecutionLogicException("Operation没有确定操作对象");
        }
        boolean isExecute = true;
        if ("base".equals(objType)) {
            resource.put("operateObj", roles.get("base"));
            resource.put("operateArm", roles.get("baseArm"));
            resource.put("operateGripper", roles.get("baseGripper"));
            isExecute = resource.get("operateArm") != null;
        } else if ("no_base".equals(objType)) {
            resource.put("operateObj", roles.get("noBase"));
            resource.put("operateArm", roles.get("noBaseArm"));
            resource.put("operateGripper", roles.get("noBaseGripper"));
            isExecute = resource.get("operateArm") != null;
        } else {  // Operation Node中的obj属性是none
            resource.put("operateObj", null);
            resource.put("operateArm", null);
            resource.put("operateGripper", null);
        }
        resource.putAll(roles);
        Map<String, Object> result = new HashMap<>();
        result.put("resource", resource);
        result.put("isExecute", isExecute);
        return result;
    }
}
