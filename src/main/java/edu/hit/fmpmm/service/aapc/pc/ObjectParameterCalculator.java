package edu.hit.fmpmm.service.aapc.pc;

import edu.hit.fmpmm.domain.exception.ExecutionLogicException;
import edu.hit.fmpmm.domain.neo4j.node.Instance;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=false)
public abstract class ObjectParameterCalculator extends ParameterCalculator {
    protected final double SAFE_DISTANCE = 0.02;  // 常用到的两个对象之间的安全距离
    protected final double SAFE_DISTANCE_LARGER = 0.03;  // 两个对象之间的安全距离，更远
    protected final double SAFE_DISTANCE_SMALLER = 0.005;  // 两个对象之间的安全距离，更近
    protected final double SAFE_DISTANCE_LIFT = 0.1;  // 抓取提起的高度
    private Map<String, Instance> resource;
    private Map<String, List<Instance>> otherCapabilities;

    public Instance getBoxSize(List<Instance> objOtherCaps) {
        Instance boxSize = null;
        for (Instance cap : objOtherCaps) {  // 从其他能力中找到外形包围盒这个特征能力
            if ("外形包围盒".equals(cap.getName())) {
                boxSize = cap;
                break;
            }
        }
        if (boxSize == null) {
            throw new ExecutionLogicException("缺少能力boxSize");
        }
        return boxSize;
    }
}
