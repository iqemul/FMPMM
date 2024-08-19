package edu.hit.fmpmm.util;

import edu.hit.fmpmm.domain.neo4j.node.Goal;
import edu.hit.fmpmm.domain.neo4j.node.Instance;
import edu.hit.fmpmm.domain.neo4j.node.Node;
import edu.hit.fmpmm.domain.web.UserMsg;
import edu.hit.fmpmm.dto.ReasoningResult;
import edu.hit.fmpmm.dto.Result;
import org.neo4j.driver.internal.value.StringValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MsgHandler {
    /**
     * 用于工艺路径规划功能的消息处理
     *
     * @param result 工艺路径规划的结果
     * @return UserMsg对象
     */
    public UserMsg processExecMsgHandler(Result result) {
        UserMsg userMsg = new UserMsg();
        StringBuilder msg = new StringBuilder();
        String title = "工艺执行完毕";
        if (result.getSuccess()) {
            @SuppressWarnings("unchecked")
            List<Map<String, List<Node>>> processes = (List<Map<String, List<Node>>>) result.getData();

            for (Map<String, List<Node>> process : processes) {
                String processName = "";
                String objsName = "";
                StringBuilder features = new StringBuilder();
                for (Map.Entry<String, List<Node>> entry : process.entrySet()) {
                    if ("process".equals(entry.getKey())) {
                        Instance node = (Instance) entry.getValue().get(0);
                        processName = node.getName();
                        Map<String, Object> otherProperties = node.getOtherProperties();
                        String baseFeature = ((StringValue) otherProperties.get("base_feature")).asString();
                        String noBaseFeature = ((StringValue) otherProperties.get("no_base_feature")).asString();
                        features.append("[")
                                .append(baseFeature).append(",").append(noBaseFeature)
                                .append("]");
                    } else if ("objs".equals(entry.getKey())) {
                        Instance obj1 = (Instance) entry.getValue().get(0);
                        Instance obj2 = (Instance) entry.getValue().get(1);
                        objsName = obj1.getName() + "和" + obj2.getName();
                    }
                }

                if (!objsName.isEmpty() && !"".equals(processName)) {
                    msg.append(objsName).append("进行了").append(processName)
                            .append("\n")
                            .append("使用到的特征能力：").append(features);
                }
            }
        } else {
            title = "工艺执行发生错误";
            msg.append(result.getErrorMsg());
        }
        userMsg.setTitle(title);
        userMsg.setMsg(msg.toString());
        userMsg.setSignal("updateMsg");
        return userMsg;
    }

    /**
     * 用于工艺推荐功能的消息处理
     *
     * @param result 工艺推荐的结果
     * @return UserMsg对象
     */
    public UserMsg processRecMsgHandler(Result result) {
        UserMsg userMsg = new UserMsg();
        if (result.getSuccess()) {
            userMsg.setTitle("工艺推荐成功");
            StringBuilder msg = new StringBuilder();
            ReasoningResult reasoningResult = (ReasoningResult) result.getData();
            List<Goal> goals = reasoningResult.getGoals();
            List<List<Instance>> robots4obj1 = reasoningResult.getRobots4obj1();
            List<List<Instance>> robots4obj2 = reasoningResult.getRobots4obj2();
            if (goals == null || goals.isEmpty()) {
                msg.append("没有推荐的工艺\n");
            } else {
                msg.append("推荐的工艺有：\n");
                for (Goal goal : goals) {
                    msg.append(goal.getName()).append("\n");
                }
            }
            if (robots4obj1 == null || robots4obj1.isEmpty()) {
                msg.append("没有为零件1推荐的机器人\n");
            } else {
                msg.append("为零件1推荐的机器人有：\n");
                for (List<Instance> robots : robots4obj1) {
                    for (Instance instance : robots) {
                        msg.append(instance.getName()).append(" ");
                    }
                    msg.append("\n");
                }
            }
            if (robots4obj2 == null || robots4obj2.isEmpty()) {
                msg.append("没有为零件2推荐的机器人\n");
            } else {
                msg.append("为零件2推荐的机器人有：\n");
                for (List<Instance> robots : robots4obj2) {
                    for (Instance instance : robots) {
                        msg.append(instance.getName()).append(" ");
                    }
                    msg.append("\n");
                }
            }
            userMsg.setMsg(msg.toString());
        } else {
            userMsg.setTitle("工艺推荐发生错误");
            userMsg.setMsg(result.getErrorMsg());
        }
        userMsg.setSignal("updateRecMsg");
        return userMsg;
    }
}
