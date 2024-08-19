package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.domain.neo4j.node.Operation;
import edu.hit.fmpmm.service.aapc.pc.RobotParameterCalculator;

import java.util.ArrayList;

public class Velocity extends RobotParameterCalculator {
    @Override
    public Object value() {
        ArrayList<Double> maxVel = new ArrayList<>();
        double[] vel = new double[]{100.0, 100.0, 100.0, 100.0};
        Operation operation = getOperation();
        if (operation != null) {
            if ("执行轴孔装配".equals(operation.getName())) {  //  || "执行卡榫装配".equals(getOperation().getName())
                vel = new double[]{8.0, 8.0, 8.0, 8.0};
            } else if ("执行卡榫装配".equals(operation.getName())) {
                vel = new double[]{40.5, 40.5, 40.5, 40.5};
            }
        }
        for (double v : vel) {
            maxVel.add(v);
        }
        return maxVel;
    }
}
