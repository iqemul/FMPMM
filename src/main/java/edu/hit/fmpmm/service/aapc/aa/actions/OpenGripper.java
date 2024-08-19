package edu.hit.fmpmm.service.aapc.aa.actions;

import co.nstant.in.cbor.CborException;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.service.aapc.aa.ActionActuator;

import java.util.Map;

public class OpenGripper extends ActionActuator {
    public OpenGripper() {
    }

    public OpenGripper(Robot robot) {
        super(robot);
    }

    @Override
    public boolean go(Map<String, Object> params) throws CborException {
        return this.robot.openGripper();
    }
}
