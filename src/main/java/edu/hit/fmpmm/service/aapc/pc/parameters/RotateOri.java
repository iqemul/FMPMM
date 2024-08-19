package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.domain.neo4j.node.Operation;
import edu.hit.fmpmm.service.aapc.pc.ObjectParameterCalculator;

public class RotateOri extends ObjectParameterCalculator {
    @Override
    public Object value() {
        Operation operation = getOperation();
        String operationName = operation.getName();
        // 我真的不会计算
        if ("将两个装配对象对齐".equals(operationName)) {
            return '+';
        }
        return '-';
    }
}
