package edu.hit.fmpmm.service.aapc.aa;

import edu.hit.fmpmm.domain.exception.ExecutionLogicException;
import edu.hit.fmpmm.domain.neo4j.node.Instance;
import edu.hit.fmpmm.domain.sim.SimClient;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.service.aapc.APFactory;
import lombok.Data;
import org.neo4j.driver.internal.value.StringValue;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Data
@Component
public class ActionFactory implements APFactory {
    private SimClient simClient;

    private Robot robot;
    private Instance robotNode;
    private Instance gripperNode;

    /**
     * 创建对应的动作（Action）实例
     *
     * @param action 动作类型，通过Action Node得到
     * @return Action类的实例
     */
    public ActionActuator createAction(String action) {
//        String className = this.getProperty(config, action, config.getActionConfigName());
        String className = this.getProperty("action", action);
        if (className == null) {
            throw new ExecutionLogicException("找不到对应的机器人动作：" + action);
        }
        Class<?> actionClass;
        try {
            actionClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            ActionActuator result = (ActionActuator) actionClass.getDeclaredConstructor().newInstance();
            if (this.robot != null) {  // **初始化robot**
                result.setRobot(robot);
            } else {
                throw new ExecutionLogicException("在 ActionFactory 生产 Action 之前没有指定机器人（Robot）");  // TODO 异常处理器
            }
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Robot getSimRobot() {
        if (robotNode == null || gripperNode == null) {
            throw new ExecutionLogicException("没有在ActionFactory中指定Arm和Gripper资源");
        }
        Robot robot = null;
        // 根据type找应该创建哪个型号机器人的对象
        Map<String, Object> armOtherPro = robotNode.getOtherProperties();
        String robotType = ((StringValue) armOtherPro.get("model")).asString();  // operateArm.getName();
//        String className = this.getProperty(robotConfig, robotType, robotConfig.getRobotConfigName());
        String className = this.getProperty("robot", robotType);
        if (className == null) {
            throw new ExecutionLogicException("找不到对应的机器人类：" + robotType);
        }
        String robotCode = ((StringValue) robotNode.getOtherProperties().get("code")).asString();
        String gripperCode = ((StringValue) gripperNode.getOtherProperties().get("code")).asString();
        if (robotCode != null && !robotCode.isEmpty()) {
            if (gripperCode == null) {
                gripperCode = "";
            }
            Class<?> robotClass;
            try {
                robotClass = Class.forName(className);
                robot = (Robot) robotClass.getDeclaredConstructor().newInstance();
                robot.setClient(simClient);
                robot.setPath("/" + robotCode);
                if (!gripperCode.isEmpty()) {
                    robot.setGripperSignal(gripperCode);
                }
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        setRobot(robot);
        return robot;
    }
}
