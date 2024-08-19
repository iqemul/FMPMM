package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.service.aapc.pc.ObjectParameterCalculator;

public class MoveRotateOri extends ObjectParameterCalculator {
    @Override
    public Object value() {
        String operationName = getOperation().getName();
        if ("执行螺纹装配".equals(operationName)) {
            return '-';
        }
        return '+';
    }
}
