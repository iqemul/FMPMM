package edu.hit.fmpmm.service.aapc.aa.actions;

import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import edu.hit.fmpmm.service.aapc.aa.ActionActuator;
import edu.hit.fmpmm.util.DataProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoveWithRotate extends ActionActuator {
    DataProcessor processor = new DataProcessor();

    @Override
    public boolean go(Map<String, Object> params) throws CborException {  // 夹爪一边旋转一边移动
        commonSettings(params);  // 速度、加速度、jerk的配置
        double angle = (double) params.get("move_rotate_theta");  // 要转多少弧度
        List<Double> startPose = robot.getTip().getPose();  // 初始的位姿
        @SuppressWarnings("unchecked")
        List<Double> endPosition = (List<Double>) params.get("position");  // 结束的位置
        char ori = (char) params.get("move_rotate_ori");  // 旋转方向
        angle = ori == '+' ? angle : -angle;  // 118.75436147590284
        // 循环cnt次，每次旋转的弧度是angle/cnt，用于生成Path
        int cnt = 30;  // 插多少个点用来生成路径
        int pointNum = 500;  // 生成路径后，这个路径由多少个点组成
        double deltaAngle = angle / cnt;
        double deltaZ = (endPosition.get(2) - startPose.get(2)) / cnt;
        boolean flag;
        // 先把夹爪的方向设置成竖直向下并且z的方向和世界参考系相反
        this.initGripperOri();
        List<Double> points = new ArrayList<>();  // 用于存路径
        // 用一个变量记录夹爪旋转的方向，即这个变量用于维护欧拉角的γ值
        double gamma = robot.getTip().getOrientation().get(2);
        for (int i = 1; i <= cnt; i++) {
            gamma = handleGamma(gamma, deltaAngle);
            List<Double> orientation = new ArrayList<>();
            orientation.add(Math.PI);
            orientation.add(0.0);
            orientation.add(gamma);
            List<Double> quaternion = processor.euler2quaternion(orientation);
            points.add(endPosition.get(0));
            points.add(endPosition.get(1));
            points.add(startPose.get(2) + deltaZ * i);
            points.addAll(quaternion);
        }
//        System.out.println("points:" + points);
        // 生成Path
        List<Integer> upVector = new ArrayList<>();
        upVector.add(0);
        upVector.add(0);
        upVector.add(1);
        RemoteAPIObjects._sim sim = robot.getClient().getSim();
        // 8对应着二进制：01000，每一位有含义 https://manual.coppeliarobotics.com/en/regularApi/simCreatePath.htm
        Long path = sim.createPath(points, 8, pointNum, 0.0, 0, upVector);
        // 机械臂末端沿着Path移动
        flag = followPath(path, sim);
        return flag;
    }

    private boolean followPath(Long path, RemoteAPIObjects._sim sim) throws CborException {
        List<Double> pathData = sim.unpackDoubleTable(
                (Object) sim.readCustomDataBlock(path, "PATH")
        );
        // 处理pathData -> position + quaternion
        List<Double> positions = new ArrayList<>();
        List<Double> quaternions = new ArrayList<>();
        for (int i = 0; i < pathData.size(); i++) {
            int val = i % 7;
            if (val <= 2) {
                positions.add(pathData.get(i));
            } else {
                quaternions.add(pathData.get(i));
            }
        }
        // 路径长度
        Object[] lengths = sim.getPathLengths(positions, 3);
        @SuppressWarnings("unchecked")
        List<Double> pathLengths = (List<Double>) lengths[0];  // 每个点对应的路径长度 pathLengths.size() == pointNum
        Double totalLength = (Double) lengths[1];  // 路径的总长度（m）
        double vel = 0.00009;  // 设定一个速度
        return robot.moveFollowPath(positions, quaternions, pathLengths, totalLength, vel);
    }

    private void initGripperOri() {
        List<Double> initOri = new ArrayList<>();
        initOri.add(Math.PI);
        initOri.add(0.0);
        initOri.add(0.0);
        List<Double> pose = robot.getTip().getPose();
        List<Double> qua = processor.euler2quaternion(initOri, false);
        robot.getTarget().setQuaternion(qua);
        int i = 3;
        for (Double e : qua) {
            pose.set(i++, e);
        }
        try {
            robot.moveByEnd(pose);
//        robot.getTarget().setPose(pose);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    private double handleGamma(double gamma, double delta) {
        return gamma + delta;
    }
}
