package edu.hit.fmpmm.service.process;

import edu.hit.fmpmm.domain.neo4j.node.*;
import edu.hit.fmpmm.repo.AbstractionRepository;
import edu.hit.fmpmm.repo.CapabilityRepository;
import edu.hit.fmpmm.repo.InstanceRepository;
import edu.hit.fmpmm.repo.OperationRepository;
import edu.hit.fmpmm.util.DataProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ProcessUtil {
    private final AbstractionRepository abstractionRepo;
    private final InstanceRepository instanceRepo;
    private final CapabilityRepository capabilityRepo;
    private final OperationRepository operationRepo;
    private final DataProcessor dataProcessor = new DataProcessor();

    public ProcessUtil(AbstractionRepository abstractionRepo, InstanceRepository instanceRepo, CapabilityRepository capabilityRepo, OperationRepository operationRepo) {
        this.abstractionRepo = abstractionRepo;
        this.instanceRepo = instanceRepo;
        this.capabilityRepo = capabilityRepo;
        this.operationRepo = operationRepo;
    }

    public boolean isProcess(Instance instance) {  // 判断instance是不是工艺实例
        // 向上追溯，不论哪条路径定能找到唯一的一个Abstraction
        // 可能的路径：include - has_instance - include\ include - has_instance\ has_instance - include\ has_instance
        Instance node1 = instance;
        List<Instance> fatherInstances = instanceRepo.findInstancesWitchInclude(node1.getId());
        while (!fatherInstances.isEmpty()) {  // 向上追溯，走0个或1个或n个include关系
            node1 = fatherInstances.get(0);
            fatherInstances = instanceRepo.findInstancesWitchInclude(node1.getId());
        }
        List<Abstraction> fatherAbstraction = abstractionRepo.findAbstractionsWhichHasInstance(node1.getId());
        if (fatherAbstraction.isEmpty()) {  // 这个实例没有概念节点，一定不属于制造工艺
            return false;
        }
        Abstraction node2 = fatherAbstraction.get(0);
        if ("制造工艺".equals(node2.getName())) {
            return true;
        } else {  // 再向上走1个或n个Abstraction之间的include
            List<Abstraction> abstraction = abstractionRepo.findAbstractionWhichInclude(node2.getId());
            while (!abstraction.isEmpty()) {
                node2 = abstraction.get(0);
                if ("制造工艺".equals(node2.getName())) {
                    return true;
                }
                abstraction = abstractionRepo.findAbstractionWhichInclude(node2.getId());
            }
            return false;
        }
    }

    /**
     * 检索instance的type类型的特征，特征能力实例
     *
     * @param instance 工件
     * @param type     特征能力类型
     * @return 特征集合
     */
    public List<Instance> findFeatureByInstanceAndType(Instance instance, String type) {
        if (instance == null) {
            return new ArrayList<>();
        }
        Capability abstraction = capabilityRepo.findCapabilityByName(type);
        List<Instance> featuresAtOne = instanceRepo.findCapabilityInstancesByHasInstance(abstraction.getId());
        List<Instance> featuresOfObj = instanceRepo.findCapabilityInstancesByOwner(instance.getId());
        List<Node> fot = new ArrayList<>(featuresOfObj);
        for (Instance fat1 : featuresAtOne) {
            List<Instance> featuresAtTwo = instanceRepo.findIncludeSons(fat1.getId());
            List<Node> fat = new ArrayList<>(featuresAtTwo);
            List<Node> feature = dataProcessor.getIntersection(fat, fot);
            if (!feature.isEmpty()) {  // 正常来说只有一个特征
                List<Instance> fl = new ArrayList<>();
                for (Node node : feature) {
                    fl.add((Instance) node);
                }
                return fl;
            }
        }
        return new ArrayList<>();
    }

    /**
     * 判断一个Goal节点是不是工艺目标
     *
     * @param goal 待判断的Goal节点
     * @return 是否是工艺目标
     */
    public boolean isProcessGoal(Goal goal) {
        // 通过判断有没有Operation节点来决定
        List<Operation> operations = operationRepo.findOperationsByGoal(goal.getId());
        return !operations.isEmpty();
    }

    public Instance findGripperByGoal(Goal grabGoal) {
        Abstraction abstraction = abstractionRepo.findAbstractionByName("夹具");
        List<Instance> grippers = abstraction.getHasInstances();  // 夹具具有的实例
        for (Instance gripper : grippers) {
            List<Goal> goals = gripper.getAchieveGoals();  // 这个实例具有的抓取目标
            for (Goal g : goals) {
                if (Objects.equals(g.getId(), grabGoal.getId())) {  // g.equals(grabGoal)这个抓取目标和给定的抓取目标一样
                    return gripper;  // 要找的就是这个夹爪实例
                }
            }
        }
        return null;
    }

    public List<Instance> findRobotArmsByGriper(Instance gripper) {
        List<Instance> installedOnRobots = gripper.getInstalledOns();
        if (installedOnRobots.isEmpty()) {
            return new ArrayList<>();
        }
        return installedOnRobots;
    }
}
