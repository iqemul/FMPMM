package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.domain.neo4j.node.Operation;
import edu.hit.fmpmm.service.aapc.pc.RobotParameterCalculator;

import java.util.ArrayList;

public class Jerk extends RobotParameterCalculator {
    @Override
    public Object value() {
        ArrayList<Double> maxJerk = new ArrayList<>();
        double[] jerk = new double[]{600.0, 600.0, 600.0, 600.0};
        Operation operation = getOperation();
        if (operation != null) {
            if ("执行轴孔装配".equals(operation.getName())) {  //  || "执行卡榫装配".equals(getOperation().getName())
                jerk = new double[]{100.0, 100.0, 100.0, 100.0};
            } else if ("执行卡榫装配".equals(operation.getName())) {
                jerk = new double[]{400.0, 400.0, 400.0, 400.0};
            }
        }

        for (double v : jerk) {
            maxJerk.add(v);
        }
        return maxJerk;
    }
}
