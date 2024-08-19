package edu.hit.fmpmm.service.process;

import edu.hit.fmpmm.domain.exception.ExecutionLogicException;
import edu.hit.fmpmm.domain.neo4j.node.Abstraction;
import edu.hit.fmpmm.domain.neo4j.node.Goal;
import edu.hit.fmpmm.domain.neo4j.node.Instance;
import edu.hit.fmpmm.domain.neo4j.node.Node;
import edu.hit.fmpmm.dto.ReasoningResult;
import edu.hit.fmpmm.dto.Result;
import edu.hit.fmpmm.repo.AbstractionRepository;
import edu.hit.fmpmm.repo.GoalRepository;
import edu.hit.fmpmm.repo.InstanceRepository;
import edu.hit.fmpmm.util.DataProcessor;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.neo4j.driver.internal.value.StringValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserReqService {  // 处理用户需求
    private final InstanceRepository instanceRepo;
    private final AbstractionRepository abstractionRepo;
    private final GoalRepository goalRepo;
    private final DataProcessor processor = new DataProcessor();
    private final ProcessUtil processUtil;

    @Autowired
    public UserReqService(
            InstanceRepository instanceRepo, AbstractionRepository abstractionRepo, GoalRepository goalRepo, ProcessUtil processUtil) {
        this.instanceRepo = instanceRepo;
        this.abstractionRepo = abstractionRepo;
        this.goalRepo = goalRepo;
        this.processUtil = processUtil;
    }

    /**
     * 把用户想要的成品转化成执行多步工艺所需的操作对象、工艺、机器人
     *
     * @param obj 成品
     * @return 工艺列表，每个元素包含：操作对象、工艺实例
     */
    public List<Map<String, List<Node>>> requirementAnalyse(String obj) {
//        List<Map<String, List<Node>>> result = new ArrayList<>();
        List<Map<String, List<Node>>> result = new ArrayList<>();
        // 通过输入的obj在FMPKG的组成视图中检索它的构成，以列出装配的顺序
        // 首先找到obj字符串代表哪个Node
        Instance objNode = findObjNodeByName(obj);
        if (objNode == null) {
            return result;
        }
        List<List<Instance>> operateNodes = findOperateNodesSeq(objNode);  // 要依次执行（装配）工艺的操作对象
        for (List<Instance> instances : operateNodes) {  // 顺序遍历，找到使用的工艺
            Goal goal = findProcessGoal(instances);

            Map<String, List<Node>> process = new HashMap<>();
            List<Node> objs = new ArrayList<>(instances);
            process.put("objs", objs);
            Instance proInstance = findProcessInstance(goal, objs);
            List<Node> goalContainer = new ArrayList<>();
            goalContainer.add(goal);
            List<Node> processContainer = new ArrayList<>();
            processContainer.add(proInstance);
            process.put("processGoal", goalContainer);  // 工艺目标
            process.put("process", processContainer);  // 工艺实例
            // 找机器人
//            List<Node> robots = findProcessRobots(instances);
            // 这组工艺添加到工艺列表
            result.add(process);
        }

        return result;
    }

    public Result processRec(Map<String, String> objs) {
        try {
            ReasoningResult reasoningResult = reasoningAnalyse(
                    objs.getOrDefault("obj1", ""), objs.getOrDefault("obj2", ""));
            return Result.success(reasoningResult);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    public ReasoningResult reasoningAnalyse(String obj1, String obj2) {
        Instance objNode1 = findObjNodeByName(obj1);
        Instance objNode2 = findObjNodeByName(obj2);
        if (objNode1 == null || objNode2 == null) {
            return new ReasoningResult(null, null, null);
        }
        List<Instance> objs = new ArrayList<>();
        objs.add(objNode1);
        objs.add(objNode2);
        return reasoningProcess(objs);
    }

    private Instance findObjNodeByName(String obj) {  // 根据字符串找到最匹配的一个装配体实例
        // 先看是不是能直接根据name找到节点
        Instance node = instanceRepo.findInstanceByName(obj);
        // 不能直接找到节点，就找装配体的实例中name相似度和obj最高的
        if (node == null) {
            Abstraction abstraction = abstractionRepo.findAbstractionByName("装配体");
            List<Instance> instances = abstraction.getHasInstances();
            double similarScore = 0.75;  // 相似度阈值
            for (Instance instance : instances) {  // 遍历装配体的实例
                String name = instance.getName();  // 这个实例的name
                Map<CharSequence, Integer> nameMap = Arrays.stream(name.split("")).collect(
                        Collectors.toMap(c -> c, c -> 1, Integer::sum));
                Map<CharSequence, Integer> objMap = Arrays.stream(obj.split("")).collect(
                        Collectors.toMap(c -> c, c -> 1, Integer::sum));
                CosineSimilarity similarity = new CosineSimilarity();
                double score = similarity.cosineSimilarity(nameMap, objMap);
                if (score > similarScore) {
                    similarScore = score;
                    node = instance;
                }
            }
        }
        return node;  // node为null，低于阈值的不能匹配
    }

    private List<List<Instance>> findOperateNodesSeq(Instance objNode) {  // 找到一个装配体的组成
        List<List<Instance>> res = new ArrayList<>();
        // 用一个栈表示待分析的节点，看节点有没有include
        LinkedList<Instance> stack = new LinkedList<>();
        stack.push(objNode);
        while (!stack.isEmpty()) {
            Instance node = stack.pop();  // 取出栈顶元素
            List<Instance> includes = node.getIncludes();  // 这里要使用Instance类实例特有的方法，所以不能用多态
            if (!includes.isEmpty()) {
                res.add(0, includes);  // 插到最前面，这是先制造（装配）的
                for (Instance include : includes) {
                    stack.push(include);  // include节点入栈后面分析
                }
            }
        }
        return res;
    }

    private Goal findProcessGoal(List<Instance> instances) {  // 根据操作对象检索工艺
        List<Goal> goals1 = instances.get(0).getAchieveGoals();
        List<Goal> goals2 = instances.get(1).getAchieveGoals();
        List<Node> g1 = new ArrayList<>(goals1);
        List<Node> g2 = new ArrayList<>(goals2);
        List<Node> intersection = processor.getIntersection(g1, g2);
        if (!intersection.isEmpty()) {
            return (Goal) intersection.get(0);
        } else {
            throw new ExecutionLogicException("匹配不到两个操作对象(" + instances + ")的工艺目标");
        }
    }

    /**
     * 根据工艺目标和操作对象得到工艺实例
     *
     * @param goal 工艺目标节点
     * @param objs 操作对象
     * @return 工艺实例
     */
    private Instance findProcessInstance(Goal goal, List<Node> objs) {
        List<Instance> instances = instanceRepo.findInstancesWhichAchieveGoal(goal.getId());  // 从这些instances中找到是制造工艺子结点的instance
        Instance process = null;
        for (Instance instance : instances) {
            for (Node obj : objs) {
                if (!Objects.equals(instance.getId(), ((Instance) obj).getId())) {
                    if (processUtil.isProcess(instance)) {
                        process = instance;
                        break;
                    }
                }
            }
        }
        if (process == null) {
            throw new ExecutionLogicException("找不到工艺实例");
        }
        return process;
    }

    public ReasoningResult reasoningProcess(List<Instance> objs) {
        List<Goal> goals = new ArrayList<>();
        // 检索两个对象的装配特征能力
        // 长度为2的列表，分别存储两个obj的assembly feature
        List<List<Instance>> objsAssFea = new ArrayList<>();
        List<List<Instance>> objsGraFea = new ArrayList<>();
        for (Instance obj : objs) {
            objsAssFea.add(processUtil.findFeatureByInstanceAndType(obj, "装配特征"));
            objsGraFea.add(processUtil.findFeatureByInstanceAndType(obj, "抓取特征"));
        }
        if (objsAssFea.size() != 2) {
            throw new ExecutionLogicException("新对象检索特征能力出现问题");
        }
        // 查看这些特征能力的兄弟都能执行什么工艺目标
        // processGoalsLikely 可能会执行的工艺目标，但是最终要筛选
        Map<Set<String>, Goal> processGoalsLikely = new HashMap<>();  // key: set of ass feature's names; value: goal
        Map<String, List<Instance>> gripperAndArmsLikely = new HashMap<>();  // ley: grab feature; value: 0 - gripper, 1 - arm
        for (List<Instance> objAssFea : objsAssFea) {
            for (Instance af : objAssFea) {
                // 检索这个特征实例的父节点fatherInstance（一级Instance）
                List<Instance> fatherInstances = instanceRepo.findInstancesWitchInclude(af.getId());
                if (fatherInstances.isEmpty()) {
                    throw new ExecutionLogicException("新对象无法检索到特征能力的父节点");
                }
                Instance fatherInstance = fatherInstances.get(0);
                // 检索这个fatherInstance的儿子节点
                List<Instance> includes = instanceRepo.findIncludeSons(fatherInstance.getId());
                // 看这些特征节点能执行什么工艺
                /* TODO * 我竟然写的是工件Instance节点能和工艺目标具有achieve_goal关系，而不是特征实例和工艺目标具有关系，烦
                    因为特征作为能力，能力和工艺目标之间不存在achieve_goal关系
                */
                for (Instance include : includes) {
                    // 看这些特征节点被谁所拥有
                    List<Instance> owners = instanceRepo.findCapabilityOwners(include.getId());
                    for (Instance owner : owners) {  // owner是拥有这个特征能力的工件Instance节点
                        List<Goal> tmpGoals = goalRepo.findGoalsByInstance(owner.getId());
                        // 找到是工艺目标的goal
                        for (Goal goal : tmpGoals) {
                            boolean isProcessGoal = processUtil.isProcessGoal(goal);
                            if (isProcessGoal) {
                                // 找到这个goal使用的工艺实例
                                Instance processInstance = findProcessInstance(goal);
                                // 这个工艺实例使用的特征
                                Map<String, Object> otherProperties = processInstance.getOtherProperties();
                                String baseFeature = ((StringValue) otherProperties.get("base_feature")).asString();
                                String noBaseFeature = ((StringValue) otherProperties.get("no_base_feature")).asString();
                                Set<String> features = new HashSet<>();
                                features.add(baseFeature);
                                features.add(noBaseFeature);
                                processGoalsLikely.put(features, goal);  // 添加进可能性集合
                            } else {  // 是抓取目标，备选出可使用的机械臂和夹具
                                // 找到owner的抓取特征
                                List<Instance> grabFeatures = processUtil.findFeatureByInstanceAndType(owner, "抓取特征");
                                if (grabFeatures.isEmpty()) {
                                    continue;
                                }
                                Instance gripper = processUtil.findGripperByGoal(goal);
                                List<Instance> arms = processUtil.findRobotArmsByGriper(gripper);
                                for (Instance arm : arms) {
                                    List<Instance> robot = new ArrayList<>();
                                    robot.add(gripper);
                                    robot.add(arm);
                                    gripperAndArmsLikely.put(grabFeatures.get(0).getName(), robot);  // TODO 因为*，所以很难表达逻辑，先这么写吧
                                }
                            }
                        }
                    }
                }
            }
        }
        // 匹配给定的两个对象能执行什么工艺目标
        for (Instance f1 : objsAssFea.get(0)) {
            for (Instance f2 : objsAssFea.get(1)) {
                Set<String> features = new HashSet<>();
                features.add(f1.getName());
                features.add(f2.getName());
                Goal likelyGoal = processGoalsLikely.getOrDefault(features, null);
                if (likelyGoal != null) {
                    goals.add(likelyGoal);
                }
            }
        }
        // 为两个对象分别推荐机器人
        List<List<Instance>> robots4obj1 = new ArrayList<>();
        List<List<Instance>> robots4obj2 = new ArrayList<>();
        for (Instance f1 : objsGraFea.get(0)) {
            List<Instance> robot = gripperAndArmsLikely.getOrDefault(f1.getName(), null);
            if (robot != null) {
                robots4obj1.add(robot);
            }
        }
        for (Instance f2 : objsGraFea.get(1)) {
            List<Instance> robot = gripperAndArmsLikely.getOrDefault(f2.getName(), null);
            if (robot != null) {
                robots4obj2.add(robot);
            }
        }
        return new ReasoningResult(goals, robots4obj1, robots4obj2);
    }

    private Instance findProcessInstance(Goal goal) {
        List<Instance> instances = instanceRepo.findInstancesWhichAchieveGoal(goal.getId());
        Instance process = null;
        for (Instance instance : instances) {
            if (processUtil.isProcess(instance)) {  // 一般一个工艺目标只会关联一个工艺实例
                process = instance;
                break;
            }
        }
        if (process == null) {
            throw new ExecutionLogicException("找不到与{%s}关联的工艺实例".formatted(goal.getName()));
        }
        return process;
    }
}
