package edu.hit.fmpmm.domain.sim.robot;

import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import edu.hit.fmpmm.domain.sim.Dummy;
import edu.hit.fmpmm.domain.sim.SimClient;
import edu.hit.fmpmm.domain.sim.SimObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class Robot extends SimObject {
    // private String id;

    private int jointNum;  // 机器人有几个关节

    private ArrayList<Integer> joints = new ArrayList<>();  // 关节的handle（不需要频繁插入删除，访问效率高，适合选用ArrayList）

    private String gripperSignal;  // 控制夹爪的信号名称

    private int gripperCloseSignal = 0;  // 夹爪关闭的信号数值

    private int gripperOpenSignal = 1;  // 夹爪张开的信号数值

    private Dummy target;  // target dummy

    private Dummy tip;  // tip dummy

    private Dummy goal;  // goal dummy

    private ArrayList<Double> maxVel = new ArrayList<>();

    private ArrayList<Double> maxAccel = new ArrayList<>();

    private ArrayList<Double> maxJerk = new ArrayList<>();
    public static SimClient _client4callback;

    public Robot() {
    }

    protected Robot(String path, int jointNum) {
        this(path, jointNum, null, "");
    }

    protected Robot(String path, int jointNum, SimClient client, String gripperSignal) {
        super(client, path);  // 提供不同机器人使用相同客户端的可能
        this.jointNum = jointNum;
        this.setJointsHandles();

        if ("".equals(gripperSignal)) {
            this.gripperSignal = this.getPath().replace("/", "");  // 默认用机器人path名
        } else {
            this.gripperSignal = gripperSignal;
        }
        if (client != null && this.getPath() != null && !this.getPath().isEmpty()) {
            setInfoNeedSimClientAndPath();
        }
    }

    protected void setInfoNeedSimClientAndPath() {
        _client4callback = getClient();
        this.target = new Dummy(this.getClient(), this.getPath() + "/target");
        this.tip = new Dummy(this.getClient(), this.getPath() + "/tip");
        this.goal = new Dummy(this.getClient(), this.getPath() + "/goal");
        if (!this.getClient().isCallbackFlag()) {
            registerCallBack();
            this.getClient().setCallbackFlag(true);
        }
        this.setJointsHandles();
        try {
            this.getClient().getSim().setStepping(true);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerCallBack() {
        this.getClient().getClient().registerCallback("configCallBack", ConfigCallBack::onCallBack);
        this.getClient().getClient().registerCallback("poseCallBack", PoseCallBack::onCallBack);
    }

    @Override
    public void setPath(String path) {  // 以防不调用构造函数时griperSignal不会被设置
        super.setPath(path);
        if (this.gripperSignal == null || gripperSignal.isEmpty()) {
            this.gripperSignal = this.getPath().replace("/", "");
        }
        if (getClient() != null && this.getPath() != null && !this.getPath().isEmpty()) {
            setInfoNeedSimClientAndPath();
        }
    }

    @Override
    public void setClient(SimClient simClient) {
        super.setClient(simClient);
        if (this.getPath() != null && !this.getPath().isEmpty()) {
            setInfoNeedSimClientAndPath();
        }
    }

    protected void setJointsHandles() {  // 得到机械臂关节的handle
        if (!joints.isEmpty()) {
            joints.clear();
        }
        for (int i = 0; i < this.jointNum; i++) {
            this.joints.add(
                    this.getObjectHandle(this.getPath() + "/joint" + (i + 1))
            );
        }
    }

    public List<Double> getJointConfig() throws CborException {  // 得到仿真中机械臂的关节位置
        List<Double> configs = new ArrayList<>();
        for (int joint : this.joints) {
            configs.add(this.getClient().getSim().getJointPosition(joint));
        }
        return configs;
    }

    protected boolean moveToConfig(List<Double> configs, double waitTime, String callBack) throws CborException {
        if (waitTime == 0) waitTime = 100.0;
//        System.out.println("configs:"+configs);
        List<Double> currentConfig = this.getJointConfig();
//        System.out.println("current configs:"+currentConfig);
        List<Double> vel = new ArrayList<>();
        List<Double> accel = new ArrayList<>();
        List<Double> jerk = new ArrayList<>();

        List<Double> currentV = new ArrayList<>(this.jointNum);
        List<Double> currentA = new ArrayList<>(this.jointNum);
        List<Double> targetV = new ArrayList<>(this.jointNum);
        List<Boolean> cyclicJoints = new ArrayList<>(this.jointNum);

        for (int i = 0; i < this.jointNum; i++) {
            vel.add(this.maxVel.get(0));
            accel.add(this.maxAccel.get(0));
            jerk.add(this.maxJerk.get(0));

            currentV.add(0.0);  //
            currentA.add(0.0);
            targetV.add(0.0);
            cyclicJoints.add(false);
        }
        boolean flag = false;

        RemoteAPIObjects._sim sim = getClient().getSim();
        double startTime = sim.getSimulationTime();
        while ((sim.getSimulationTime() - startTime) < waitTime) {  // TODO 什么破东西竟然还自定义仿真时间
            sim.moveToConfig(
                    -1, currentConfig, currentV, currentA,
                    vel, accel, jerk,
                    configs, targetV,
                    callBack, joints, cyclicJoints, 0.0
            );
            currentConfig = this.getJointConfig();
            // configs基本达到目标位置可退出
            int cnt = 0;
            for (int i = 0; i < configs.size(); i++) {
                if (Math.abs(currentConfig.get(i) - configs.get(i)) < 0.0001) {
                    cnt++;
                }
            }
            if (cnt == jointNum) {
                flag = true;  // 这个的条件是给定的初始仿真时间内一定要完成任务，然后自动退出返回true；否则等仿真时间流逝完退出返回false
                break;
            }
            sim.step();
        }
        System.out.println("至此仿真运行时间：" + sim.getSimulationTime() + "S.");
        return flag;
    }

    protected boolean moveToPose(List<Double> pose, double waitTime) throws CborException {
        RemoteAPIObjects._sim sim = getClient().getSim();
        boolean flag = false;

        double startTime = sim.getSimulationTime();
        while ((sim.getSimulationTime() - startTime) < waitTime) {
            List<Double> tipPose = this.tip.getPose();
            sim.moveToPose(
                    -1, tipPose, this.maxVel, this.maxAccel, this.maxJerk,
                    pose, "poseCallBack@func", target.getHandle()
            );
            // 判断任务有没有完成
            int cnt = 0;
            for (int i = 0; i < 3; i++) {
                if (Math.abs(tipPose.get(i) - pose.get(i)) < 0.001) {  // xyz各方向上相差不大于0.8mm
                    cnt++;
                }
            }
            for (int i = 3; i < 7; i++) {
                if (Math.abs(tipPose.get(i) - pose.get(i)) < 0.0025) {  // ori
                    cnt++;
                }
            }
            if (cnt == pose.size()) {
                flag = true;
                break;
            }
            sim.step();
        }
        System.out.println("至此仿真运行时间：" + sim.getSimulationTime() + "S.");
        return flag;
    }

    /*public void setIK() throws CborException {
        RemoteAPIObjects._simIK simIK = getClient().getSimIK();
        System.out.println(simIK);
        Object[] rets = getClient().getClient().call("simIK.createEnvironment", null);
        Long ikEnvironment = (Long) rets[0];  //simIK.createEnvironment(0);
        Long group = simIK.createGroup(ikEnvironment);
        simIK.setGroupCalculation(ikEnvironment, group, RemoteAPIObjects._sim.ik_pseudo_inverse_method, 0.1, 100);
        Object[] ikRes = simIK.addElementFromScene(
                ikEnvironment, group, getHandle(), tip.getHandle(), target.getHandle(), RemoteAPIObjects._sim.ik_alpha_beta_constraint
        );  // TODO
        System.out.println(Arrays.toString(ikRes));
        Map<Integer, Integer> sim2IKMap = (Map<Integer, Integer>) ikRes[1];
        System.out.println(sim2IKMap);
        List<Integer> ikJoints = new ArrayList<>();
        for (int joint : joints) {
            ikJoints.add(sim2IKMap.get(joint));
        }
        int ikTipHandle = sim2IKMap.get(tip.getHandle());
    }*/

    private static class ConfigCallBack {
        public static Object[] onCallBack(Object[] objects) {
//            System.out.println(Arrays.toString(objects));
            RemoteAPIObjects._sim sim = _client4callback.getSim();
            @SuppressWarnings("unchecked")
            List<Double> configs = (List<Double>) objects[0];
            @SuppressWarnings("unchecked")
            List<Object> joints = (List<Object>) objects[3];
            try {
                int i = 0;
                for (Object joint : joints) {
                    if (sim.isDynamicallyEnabled(joint)) {
                        sim.setJointTargetPosition(joint, configs.get(i));
                    } else {
                        sim.setJointPosition(joint, configs.get(i));
                    }
                    i++;
                }
            } catch (CborException e) {
                throw new RuntimeException(e);
            }
//            _target4callback.setPose(_tip4callback.getPose());
            return new Object[]{1};
        }
    }

    private static class PoseCallBack {
        public static Object[] onCallBack(Object[] objects) {  // currentPose, currentVel, currentAccel, data
            RemoteAPIObjects._sim sim = _client4callback.getSim();
            @SuppressWarnings("unchecked")
            List<Double> currentPose = (List<Double>) objects[0];
            Long targetHandle = (Long) objects[3];
            try {
                sim.setObjectPose(targetHandle, currentPose);
            } catch (CborException e) {
                throw new RuntimeException(e);
            }
            Object[] tmp = new Object[1];
            tmp[0] = 1;
            return tmp;
        }
    }

    /**
     * 闭合夹爪
     *
     * @return 执行结果
     * @throws CborException cbor库的自定义异常
     */
    public boolean closeGripper() throws CborException {
        RemoteAPIObjects._sim sim = getClient().getSim();
        sim.setInt32Signal(this.gripperSignal, this.gripperCloseSignal);
        sim.wait(2.5);
        return sim.getInt32Signal(this.gripperSignal) == this.gripperCloseSignal;
    }

    public boolean openGripper() throws CborException {
        RemoteAPIObjects._sim sim = getClient().getSim();
        sim.setInt32Signal(this.gripperSignal, this.gripperOpenSignal);
        sim.wait(0.5);
        return sim.getInt32Signal(this.gripperSignal) == this.gripperOpenSignal;
    }

    /**
     * 翻转机械臂。不同机器人的翻转方式不同，规定重写
     *
     * @return 执行结果
     */
    public abstract boolean invert() throws CborException;

    public abstract boolean rotate(double theta) throws CborException;

    public boolean moveByEnd(List<Double> pose) throws CborException {
        if (pose.size() != 7) {
            return false;
        }
        return this.moveToPose(pose, 65.0);
    }

    public abstract boolean moveFollowPath(List<Double> positions, List<Double> quaternions, List<Double> pathLengths, double totalLength, double vel) throws CborException;
}
