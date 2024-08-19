package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.domain.neo4j.node.Instance;
import edu.hit.fmpmm.service.aapc.pc.ObjectParameterCalculator;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.IntegerValue;

import java.util.Map;

public class MoveRotateTheta extends ObjectParameterCalculator {
    @Override
    public Object value() {
        // (外螺纹长度 - 内螺纹长度) / 螺距 * Math.PI * 2
        Map<String, Instance> resource = getResource();
        Instance noBaseAssemFeature = resource.get("noBaseAssemFeature");
        Instance baseAssemFeature = resource.get("baseAssemFeature");
        double noHeight;
        double pitch;
        double baseHeight;
        try {
            try {
                noHeight = ((IntegerValue) noBaseAssemFeature.getOtherProperties().get("螺纹长度")).asDouble();
            } catch (ClassCastException e) {
                noHeight = ((FloatValue) noBaseAssemFeature.getOtherProperties().get("螺纹长度")).asDouble();
            }
            try {
                pitch = ((FloatValue) noBaseAssemFeature.getOtherProperties().get("螺距")).asDouble();
            } catch (Exception e) {
                pitch = ((IntegerValue) noBaseAssemFeature.getOtherProperties().get("螺距")).asDouble();
            }
            try {
                baseHeight = ((IntegerValue) baseAssemFeature.getOtherProperties().get("螺纹长度")).asDouble();
            } catch (Exception e) {
                baseHeight = ((FloatValue) baseAssemFeature.getOtherProperties().get("螺纹长度")).asDouble();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (baseHeight - noHeight) / pitch * Math.PI * 2;
    }
}
