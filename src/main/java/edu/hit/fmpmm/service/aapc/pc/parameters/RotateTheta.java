package edu.hit.fmpmm.service.aapc.pc.parameters;

import edu.hit.fmpmm.domain.neo4j.node.Instance;
import edu.hit.fmpmm.domain.sim.Dummy;
import edu.hit.fmpmm.domain.sim.SimClient;
import edu.hit.fmpmm.domain.sim.SimObject;
import edu.hit.fmpmm.service.aapc.pc.ObjectParameterCalculator;
import org.neo4j.driver.internal.value.StringValue;

import java.util.List;
import java.util.Map;

public class RotateTheta extends ObjectParameterCalculator {  // 旋转的弧度
    @Override
    public Object value() {
        Map<String, Instance> resources = getResource();
        SimClient simClient = getSimClient();
        Instance operateObjNode = resources.get("operateObj");
        Instance notOperateObjNode;
        Instance baseObjNode = resources.get("base");
        Instance noBaseObjNode = resources.get("noBase");
        if (operateObjNode == baseObjNode) {
            notOperateObjNode = noBaseObjNode;
        } else {
            notOperateObjNode = baseObjNode;
        }

        String OperateObjName = ((StringValue) operateObjNode.getOtherProperties().get("code")).asString();
        SimObject operateObj = new SimObject(simClient, "/" + OperateObjName);
        String notOperateObjName = ((StringValue) notOperateObjNode.getOtherProperties().get("code")).asString();
        SimObject notOperateObj = new SimObject(simClient, "/" + notOperateObjName);
        // 默认两个对象上各有2个dummy：p1、p2
        Dummy operateP1 = new Dummy(simClient, operateObj.getPath() + "/p1");
        Dummy operateP2 = new Dummy(simClient, operateObj.getPath() + "/p2");
        Dummy notOperateP1 = new Dummy(simClient, notOperateObj.getPath() + "/p1");
        Dummy notOperateP2 = new Dummy(simClient, notOperateObj.getPath() + "/p2");
        List<Double> op1Position = operateP1.getPosition();
        List<Double> op2Position = operateP2.getPosition();
        List<Double> nop1Position = notOperateP1.getPosition();
        List<Double> nop2Position = notOperateP2.getPosition();
        double[] vector1 = new double[3];
        double[] vector2 = new double[3];
        for (int i = 0; i < 3; i++) {
            vector1[i] = op1Position.get(i) - op2Position.get(i);
            vector2[i] = nop1Position.get(i) - nop2Position.get(i);
        }
        // 计算两个向量的点积
        double dotProduct = 0.0;
        for (int i = 0; i < 3; i++) {
            dotProduct += vector1[i] * vector2[i];
        }
        double magnitude1 = magnitude(vector1);
        double magnitude2 = magnitude(vector2);
        double cosTheta = dotProduct / (magnitude1 * magnitude2);
        return Math.acos(cosTheta);
    }

    private double magnitude(double[] vector) {  // 向量的模
        double magnitudeSquared = 0;
        for (double e : vector) {
            magnitudeSquared += Math.pow(e, 2);
        }
        return Math.sqrt(magnitudeSquared);
    }
}
