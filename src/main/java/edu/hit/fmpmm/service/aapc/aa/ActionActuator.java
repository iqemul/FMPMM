package edu.hit.fmpmm.service.aapc.aa;

import co.nstant.in.cbor.CborException;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import lombok.Data;

import java.util.ArrayList;
import java.util.Map;

@Data
public abstract class ActionActuator {
    protected Robot robot;

    public ActionActuator() {
    }

    public ActionActuator(Robot robot) {
        this.robot = robot;
    }

    /**
     * 执行动作
     */
    public abstract boolean go(Map<String, Object> params) throws CborException;

    protected void commonSettings(Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        ArrayList<Double> velocity = (ArrayList<Double>) params.get("velocity");
        robot.setMaxVel(velocity);
        @SuppressWarnings("unchecked")
        ArrayList<Double> accel = (ArrayList<Double>) params.get("accel");
        robot.setMaxAccel(accel);
        @SuppressWarnings("unchecked")
        ArrayList<Double> jerk = (ArrayList<Double>) params.get("jerk");
        robot.setMaxJerk(jerk);
    }

}
