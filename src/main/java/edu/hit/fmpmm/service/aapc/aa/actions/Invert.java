package edu.hit.fmpmm.service.aapc.aa.actions;

import co.nstant.in.cbor.CborException;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.service.aapc.aa.ActionActuator;

import java.util.Map;

public class Invert extends ActionActuator {
    public Invert() {
    }

    public Invert(Robot robot) {
        super(robot);
    }

    @Override
    public boolean go(Map<String, Object> params) throws CborException {
        commonSettings(params);
        boolean flag = robot.invert();
        this.robot.getTarget().setPose(this.robot.getTip().getPose());
        return flag;
    }
}
