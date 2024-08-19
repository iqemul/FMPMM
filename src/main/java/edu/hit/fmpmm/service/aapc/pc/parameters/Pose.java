package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.domain.neo4j.node.Instance;
import edu.hit.fmpmm.domain.neo4j.node.Operation;
import edu.hit.fmpmm.domain.sim.Dummy;
import edu.hit.fmpmm.domain.sim.SimObject;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.service.aapc.pc.ObjectParameterCalculator;
import edu.hit.fmpmm.util.DataProcessor;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.StringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Pose extends ObjectParameterCalculator {
    @Override
    public Object value() {
        List<Double> pose = new ArrayList<>();
        Map<String, Instance> resource = getResource();
        Instance operateObjNode = resource.get("operateObj");
        Map<String, List<Instance>> otherCaps = getOtherCapabilities();
        Robot robot = getRobot();
        DataProcessor processor = new DataProcessor();

        Operation operation = getOperation();
        String operationName = operation.getName();
        if (operationName.contains("抓取")) {  // 如果是抓取操作，是操作对象的位置
            Map<String, Object> otherPro = operateObjNode.getOtherProperties();
            SimObject operateObj = new SimObject(getSimClient(), "/" + ((StringValue) otherPro.get("code")).asString());
            List<Double> position = operateObj.getPosition();
            List<Instance> operateObjOtherCaps;
            Instance grabCap;
            if (Objects.equals(operateObjNode.getId(), resource.get("base").getId())) {  // 正在操作的对象是base obj
                operateObjOtherCaps = otherCaps.get("baseOtherFeatures");
                grabCap = resource.get("baseGrabFeature");
            } else {  // 正在操作的对象是no base obj。考虑这两种情况
                operateObjOtherCaps = otherCaps.get("noBaseOtherFeatures");
                grabCap = resource.get("noBaseGrabFeature");
            }
            Instance boxSize = getBoxSize(operateObjOtherCaps);
            double objHeight = ((IntegerValue) boxSize.getOtherProperties().get("高度")).asDouble() / 1000.0;
            double grabHeight = ((IntegerValue) grabCap.getOtherProperties().get("高度")).asDouble() / 1000.0;
            position.set(2, position.get(2) + objHeight / 2 - grabHeight);  // 上表面高度 - 抓取高度
            // 看方向
            List<Double> tipPosition = robot.getTip().getPosition();
            List<Double> ori = robot.getTip().getOrientation();
            if (tipPosition.get(2) > position.get(2)) {  // 要往下走
                ori.set(0, 180.0);
                ori.set(1, 0.0);
            }
            List<Double> quaOri = processor.euler2quaternion(ori, true);
            pose.addAll(position);
            pose.addAll(quaOri);
            // 如果夹具是吸盘，达到的高度是对象上表面的高度 TODO 240704
            Instance gripper = resource.getOrDefault("operateGripper", null);
            if (gripper != null && "suction pad".equals(((StringValue) gripper.getOtherProperties().get("model")).asString())) {
                pose.set(2, pose.get(2) + grabHeight);
            }
            if ("move_above_obj".equals(getAction().getName())) {
                pose.set(2, pose.get(2) + SAFE_DISTANCE);
            }
        } else if ("将基准装配对象提起".equals(operationName)) {
            pose = robot.getTip().getPose();
            pose.set(2, pose.get(2) + 0.1);
//            System.out.println(pose);
        } else if ("将基准装配对象翻转".equals(operationName)) {
            pose = robot.getTip().getPose();
            pose.set(2, pose.get(2) - 0.2);
        } else if ("将非基准装配对象提起".equals(operationName)) {
            // 基准装配对象的高度作为参考
            List<Double> baseObjPose = new SimObject(
                    getSimClient(),
                    "/" + ((StringValue) resource.get("base").getOtherProperties().get("code")).asString()
            ).getPose();
            // 当前robot的tip位置
            pose = robot.getTip().getPose();
            pose.set(2, baseObjPose.get(2) + 0.1); //TODO 240704
//            pose.set(2, baseObjPose.get(2) + 0.25);
        } else if ("将两个装配对象对齐".equals(operationName)) {  // 非基准向基准对齐
            // base obj 's position
            List<Double> basePose;
            if (resource.getOrDefault("baseArm", null) != null) {
                basePose = new SimObject(
                        getSimClient(),
                        "/" + ((StringValue) resource.get("baseArm").getOtherProperties().get("code")).asString() + "/tip"
                ).getPose();
            } else {
                Dummy baseObj = new Dummy(
                        getSimClient(),
                        "/" + ((StringValue) resource.get("base").getOtherProperties().get("code")).asString() + "/p"
                );
                basePose = baseObj.getPose();
            }
            Vector3D basePosition = new Vector3D(basePose.get(0), basePose.get(1), basePose.get(2));
            Rotation baseRotation = new Rotation(basePose.get(4), basePose.get(5), basePose.get(6), basePose.get(3), false);

            double dis = getDistance4alignObj();
//            double safeDepth = -dis;  // -0.09;  // 对齐后两个点之间的距离
            Vector3D v = basePosition.negate();  // 这个向量取反，相当于移动完机器人后tip到noBaseTip的向量
            double angle = Math.atan2(v.getY(), v.getX());
            Rotation rotation = new Rotation(Vector3D.PLUS_K, angle, RotationConvention.VECTOR_OPERATOR);
//            Vector3D alignedVector = rotation.applyTo(v);
            // 对齐后tip的坐标
//            Vector3D noBaseTipPosition = alignedVector.scalarMultiply(safeDepth).add(basePosition);
            // noBaseTip的四元数
            Rotation noBaseTipRotation = new Rotation(baseRotation.getQ0(), -baseRotation.getQ1(), -baseRotation.getQ2(), -baseRotation.getQ3(), false);
            List<Double> position = calPosition4alignObj(basePose, -dis);
            pose.addAll(position);
//            pose.add(noBaseTipPosition.getX());
//            pose.add(noBaseTipPosition.getY());
//            pose.add(noBaseTipPosition.getZ());
            pose.add(noBaseTipRotation.getQ1());
            pose.add(noBaseTipRotation.getQ2());
            pose.add(noBaseTipRotation.getQ3());
            pose.add(noBaseTipRotation.getQ0());
            /*List<Double> baseObjPosition = new SimObject(
                    getSimClient(),
                    "/" + ((StringValue) resource.get("base").getOtherProperties().get("code")).asString()
            ).getPosition();
            pose = robot.getTip().getPose();
            pose.set(0, basePose.get(0));  // 不是在xy平面内对齐，而是与基准对象自己形成的那个平面
            pose.set(1, basePose.get(1));
            pose.set(2, basePose.get(2) + dis);*/  // 0.07
        } else if ("执行轴孔装配".equals(operationName)) {
            pose = robot.getTip().getPose();
            pose.set(2, pose.get(2) - 0.03);
        } else if ("释放非基准装配对象".equals(operationName)) {
            pose = robot.getGoal().getPose();
        }  else if ("执行卡榫装配".equals(operationName)) {
            pose = robot.getTip().getPose();
            pose.set(2, pose.get(2) - 0.039);
        } else if ("将两个执行螺纹装配的对象对齐".equals(operationName)) {
            Instance noOperateObjNode = resource.get("noBase");
            List<Instance> operateObjOtherCaps = otherCaps.get("baseOtherFeatures");
            List<Instance> noOperateObjOtherCaps = otherCaps.get("noBaseOtherFeatures");
            Instance grabCap = resource.get("baseGrabFeature");
            if (operateObjNode == resource.get("noBase")) {
                noOperateObjNode = resource.get("base");
                operateObjOtherCaps = otherCaps.get("noBaseOtherFeatures");
                noOperateObjOtherCaps = otherCaps.get("baseOtherFeatures");
                grabCap = resource.get("noBaseGrabFeature");
            }
            Map<String, Object> otherPro = noOperateObjNode.getOtherProperties();
            SimObject noOperateObj = new SimObject(getSimClient(), "/" + ((StringValue) otherPro.get("code")).asString());
            pose = noOperateObj.getPose();
            // 按照上下对齐粗略计算一下：安全距离 + 正在操作对象的高 - 正在操作对象的抓取特征的高度 + 参照对象高度的一半
            Instance boxSize = getBoxSize(operateObjOtherCaps);
            Instance noBoxSize = getBoxSize(noOperateObjOtherCaps);
            double opObjHeight = ((IntegerValue) boxSize.getOtherProperties().get("高度")).asDouble() / 1000.0;
            double noOpObjHeight = ((IntegerValue) noBoxSize.getOtherProperties().get("高度")).asDouble() / 1000.0;
            double grabHeight = ((IntegerValue) grabCap.getOtherProperties().get("高度")).asDouble() / 1000.0;
            pose.set(2, pose.get(2) + SAFE_DISTANCE + opObjHeight - grabHeight + noOpObjHeight / 2);
        } else if ("执行螺纹装配".equals(operationName)) {
            pose = robot.getTip().getPose();
//            pose.set(2, pose.get(2) - SAFE_DISTANCE / 2);
        }
        return pose;
    }

    private double getDistance4alignObj() {  // 对齐两个对象时，设置两个对象之间的距离
        Map<String, Instance> resource = getResource();
        Map<String, List<Instance>> otherCaps = getOtherCapabilities();
        List<Instance> operateObjOtherCaps = otherCaps.get("baseOtherFeatures");
        Instance operateObjNode = resource.get("operateObj");
        Instance opGrabCap = resource.get("baseGrabFeature");
        if (operateObjNode == resource.get("noBase")) {
            operateObjOtherCaps = otherCaps.get("noBaseOtherFeatures");
            opGrabCap = resource.get("noBaseGrabFeature");
        }
        Instance boxSize = getBoxSize(operateObjOtherCaps);
        double opObjHeight = ((IntegerValue) boxSize.getOtherProperties().get("高度")).asDouble() / 1000.0;
        double opGrabHeight = ((IntegerValue) opGrabCap.getOtherProperties().get("高度")).asDouble() / 1000.0;

        // 如果夹具是吸盘，再加上 grab feature 's height TODO 240704
        Instance gripper = resource.getOrDefault("operateGripper", null);
        if (gripper != null && "suction pad".equals(((StringValue) gripper.getOtherProperties().get("model")).asString())) {
            return opObjHeight + opGrabHeight - 0.005;  // + 0.05
        }
        return opObjHeight - opGrabHeight + SAFE_DISTANCE_LARGER;
//        return opObjHeight + opGrabHeight + 0.045;
    }

    private List<Double> calPosition4alignObj(List<Double> pose, double dis) {
        // 空间参考系中点p的坐标
        Vector3D p = new Vector3D(pose.get(0), pose.get(1), pose.get(2));
        // p的四元数
        Quaternion rotationQuaternion = new Quaternion(pose.get(4), pose.get(5), pose.get(6), pose.get(3));
        // p沿自身z轴正方向移动后的新坐标
        Vector3D moveVector = new Vector3D(0, 0, dis);  // p沿自身z轴正方向移动的距离dis
        Vector3D point_ = rotateVector(rotationQuaternion, moveVector).add(p);
        List<Double> point = new ArrayList<>();
        point.add(point_.getX());
        point.add(point_.getY());
        point.add(point_.getZ());
        return point;
    }

    // 根据四元数对向量进行旋转变换
    private static Vector3D rotateVector(Quaternion quaternion, Vector3D vector) {
        Quaternion qVector = new Quaternion(0, vector.getX(), vector.getY(), vector.getZ());
        Quaternion result = quaternion.multiply(qVector).multiply(quaternion.getConjugate());
        return new Vector3D(result.getQ1(), result.getQ2(), result.getQ3());
    }
}
