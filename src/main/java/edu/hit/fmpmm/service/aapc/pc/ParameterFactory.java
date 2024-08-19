package edu.hit.fmpmm.service.aapc.pc;

import edu.hit.fmpmm.domain.exception.ExecutionLogicException;
import edu.hit.fmpmm.domain.neo4j.node.Action;
import edu.hit.fmpmm.domain.neo4j.node.Instance;
import edu.hit.fmpmm.domain.neo4j.node.Operation;
import edu.hit.fmpmm.domain.sim.SimClient;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.service.aapc.APFactory;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@Data
@Component
public class ParameterFactory implements APFactory {
    private SimClient simClient;
    private Map<String, Instance> resource;  // 计算参数要用到的资源
    private Map<String, List<Instance>> otherCapabilities;
    private Robot robot;
    private Operation operation;
    private Action action;


    public ParameterCalculator createParameter(String parameterName) {
//        String className = this.getProperty(parameterConfig, parameterName, parameterConfig.getParameterConfigName());
        String className = this.getProperty("parameter", parameterName);
        if (className == null) {
            throw new ExecutionLogicException("找不到对应的参数：" + parameterName);
        }
        Class<?> parameterClass;
        try {
            parameterClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Class<?> superClass = parameterClass.getSuperclass();
        if (superClass == ObjectParameterCalculator.class) {
            try {
                ObjectParameterCalculator objectParameter = (ObjectParameterCalculator) parameterClass.getDeclaredConstructor().newInstance();
                if (resource != null && otherCapabilities != null && simClient != null  && operation != null && robot != null) {
                    objectParameter.setResource(resource);
                    objectParameter.setOtherCapabilities(otherCapabilities);
                    objectParameter.setSimClient(simClient);
                    objectParameter.setOperation(operation);
                    objectParameter.setAction(action);
                    objectParameter.setRobot(robot);
                    return objectParameter;
                } else {
                    throw new ExecutionLogicException(
                            "在 ParameterFactory 生产 ObjectParameter 之前没有初始化资源" +
                                    "（Resource、otherCapabilities、SimClient、Operation、Robot）");
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else if (superClass == RobotParameterCalculator.class) {
            try {
                RobotParameterCalculator robotParameter = (RobotParameterCalculator) parameterClass.getDeclaredConstructor().newInstance();
                if (robot != null && operation != null) {
                    robotParameter.setRobot(robot);
                    robotParameter.setOperation(operation);
                    return robotParameter;
                } else {
                    throw new ExecutionLogicException("在 ParameterFactory 生产 RobotParameter 之前没有初始化资源（Robot、Operation）");
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new ExecutionLogicException(parameterName + "参数类的类型有误");
        }
    }
}
