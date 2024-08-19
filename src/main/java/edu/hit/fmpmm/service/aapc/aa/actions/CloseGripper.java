package edu.hit.fmpmm.service.aapc.aa.actions;

import co.nstant.in.cbor.CborException;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.service.aapc.aa.ActionActuator;

import java.util.Map;

public class CloseGripper extends ActionActuator {
    public CloseGripper() {
    }

    public CloseGripper(Robot robot) {
        super(robot);
    }

    @Override
    public boolean go(Map<String, Object> params) throws CborException {
        return this.robot.closeGripper();
    }
}
