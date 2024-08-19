package edu.hit.fmpmm.service.aapc.pc;

import edu.hit.fmpmm.domain.neo4j.node.Action;
import edu.hit.fmpmm.domain.neo4j.node.Operation;
import edu.hit.fmpmm.domain.sim.SimClient;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import lombok.Data;

@Data
public abstract class ParameterCalculator {
    private SimClient simClient;
    private Operation operation;
    private Action action;
    private Robot robot;

    public abstract Object value();  // 返回值，或许需要键值对，之后再说
}
