package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.domain.neo4j.node.Instance;
import edu.hit.fmpmm.domain.neo4j.node.Operation;
import edu.hit.fmpmm.domain.sim.SimObject;
import edu.hit.fmpmm.service.aapc.pc.ObjectParameterCalculator;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.StringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Position extends ObjectParameterCalculator {
    @Override
    public Object value() {
        List<Double> position = new ArrayList<>();
        Operation operation = getOperation();
        String operationName = operation.getName();
        Map<String, Instance> resource = getResource();

        if ("执行螺纹装配".equals(operationName)) {
            // 是螺母的上表面
            Instance noOperateObjNode = resource.get("noBase");
            Instance operateObjNode = resource.get("operateObj");
            Map<String, List<Instance>> otherCaps = getOtherCapabilities();
            List<Instance> noOperateObjOtherCaps = otherCaps.get("noBaseOtherFeatures");
            if (operateObjNode == resource.get("noBase")) {
                noOperateObjNode = resource.get("base");
                noOperateObjOtherCaps = otherCaps.get("baseOtherFeatures");
            }
            Instance boxSize = getBoxSize(noOperateObjOtherCaps);
            double objHeight = ((IntegerValue) boxSize.getOtherProperties().get("高度")).asDouble() / 1000.0;
            SimObject object = new SimObject(getSimClient(), "/" + ((StringValue) noOperateObjNode.getOtherProperties().get("code")).asString());
            position = object.getPosition();
            // 按照对象是竖直的
            position.set(2, position.get(2) + objHeight);  // position.get(2) + objHeight / 2
        }
        return position;
    }
}
