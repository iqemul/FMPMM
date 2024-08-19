package edu.hit.fmpmm.dto;

import edu.hit.fmpmm.domain.neo4j.node.Goal;
import edu.hit.fmpmm.domain.neo4j.node.Instance;
import lombok.Data;

import java.util.List;

/**
 * 工艺推荐要返回的推理结果
 */
@Data
public class ReasoningResult {
    private List<Goal> goals;  // 推荐的工艺
    private List<List<Instance>> robots4obj1;  // 为obj1推荐的机器人
    private List<List<Instance>> robots4obj2;  // 为obj2推荐的机器人

    public ReasoningResult(List<Goal> goals, List<List<Instance>> robots4obj1, List<List<Instance>> robots4obj2) {
        this.goals = goals;
        this.robots4obj1 = robots4obj1;
        this.robots4obj2 = robots4obj2;
    }
}
