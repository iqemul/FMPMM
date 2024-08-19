package edu.hit.fmpmm.domain.sim.robot.instance;

import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import edu.hit.fmpmm.domain.sim.Dummy;
import edu.hit.fmpmm.domain.sim.robot.Robot;

import java.util.ArrayList;
import java.util.List;

public abstract class UR extends Robot {
    public UR() {
        super("", 6);
    }

    @Override
    public boolean invert() throws CborException {
        List<Double> jointConfigs = this.getJointConfig();
        double fifthConfig = jointConfigs.get(4);
        if (fifthConfig > 0) {
            jointConfigs.set(4, fifthConfig - Math.PI);
        } else {
            jointConfigs.set(4, fifthConfig + Math.PI);
        }
        return this.moveToConfig(jointConfigs, 50.0, "configCallBack@func");
    }

    @Override
    public boolean rotate(double theta) throws CborException {
        // 机器人的最后一个关节顺/逆时针旋转theta
        List<Double> configs = this.getJointConfig();
        configs.set(5, configs.get(5) + theta);

        return this.moveToConfig(configs, 70.0, "configCallBack@func");
    }

    @Override
    public boolean moveFollowPath(
            List<Double> positions, List<Double> quaternions, List<Double> pathLengths, double totalLength, double vel
    ) throws CborException {
        double posAlongPath = 0.0;  // 已经走过的路程
        RemoteAPIObjects._sim sim = getClient().getSim();
        double startTime = sim.getSimulationTime();
        double curTime = sim.getSimulationTime();
        double preTime = 0.0;
        boolean flag = false;

        List<Integer> types = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            types.add(2);
        }
        double readyTime = totalLength / vel + 10;
        while ((curTime - startTime) < readyTime) {
            posAlongPath += vel * (curTime - preTime);
            if (posAlongPath >= totalLength * 0.95) {  // 只走路程的95% 真的可恶，因为最后离得太近容易把螺丝弄掉
                flag = true;
                break;
            }
            List<Double> pos = sim.getPathInterpolatedConfig(positions, pathLengths, posAlongPath);
            List<Double> qua = sim.getPathInterpolatedConfig(quaternions, pathLengths, posAlongPath, new Object(), types);

            // 使用IK模式，设置机器人target的pose
            Dummy target = getTarget();
            target.setPosition(pos);
            target.setQuaternion(qua);

            preTime = curTime;  // 上一步的仿真时间
            curTime = sim.getSimulationTime();
            if (vel < 0.0002) {
                vel += 0.000001;
            }
            sim.step();
        }
        return flag;
    }
}
