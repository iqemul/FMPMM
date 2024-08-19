package edu.hit.fmpmm.service.aapc.aa.actions;

import co.nstant.in.cbor.CborException;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.service.aapc.aa.ActionActuator;

import java.util.List;
import java.util.Map;

public class MoveByEnd extends ActionActuator {  // 通过指定末端位置移动
    public MoveByEnd() {
    }

    public MoveByEnd(Robot robot) {
        super(robot);
    }

    @Override
    public boolean go(Map<String, Object> params) throws CborException {
        commonSettings(params);
        Object tmpPose = params.get("pose");
        if (tmpPose == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        List<Double> pose = (List<Double>) tmpPose;
        return robot.moveByEnd(pose);
    }
}
