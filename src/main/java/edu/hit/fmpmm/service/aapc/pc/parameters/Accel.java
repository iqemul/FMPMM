package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.domain.neo4j.node.Operation;
import edu.hit.fmpmm.service.aapc.pc.RobotParameterCalculator;

import java.util.ArrayList;

public class Accel extends RobotParameterCalculator {
    @Override
    public Object value() {
        ArrayList<Double> maxAccel = new ArrayList<>();
        double[] accel = new double[]{20.0, 20.0, 20.0, 60.0};
        Operation operation = getOperation();
        if (operation != null) {
            if ("执行轴孔装配".equals(operation.getName())) {
                accel = new double[]{2.0, 2.0, 2.0, 5.0};
            } else if ("执行卡榫装配".equals(operation.getName())) {  // || "执行卡榫装配".equals(getOperation().getName())
                accel = new double[]{10.0, 10.0, 10.0, 20.3};
            }
        }

        for (double v : accel) {
            maxAccel.add(v);
        }
        return maxAccel;
    }
}
