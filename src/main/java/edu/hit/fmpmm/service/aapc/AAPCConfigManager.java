package edu.hit.fmpmm.service.aapc;

import edu.hit.fmpmm.domain.exception.ExecutionLogicException;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 单例模式 用于保证动作执行器配置文件、参数计算器配置文件、机器人配置文件只加载一次
 */
@Getter  // getActionActProperties & getParamCalProperties & getRobotProperties
public enum AAPCConfigManager {
    AA_PC_CONFIG;  // 包含配置文件信息的枚举实例

    private final Properties actionActProperties;
    private final Properties paramCalProperties;
    private final Properties robotProperties;

    AAPCConfigManager() {

        try {
            InputStream applicationInputStream = getClass().getClassLoader().getResourceAsStream("application.properties");
            Properties applicationProperties = new Properties();
            applicationProperties.load(applicationInputStream);

            String actionConfigName = applicationProperties.getProperty("action.config.file");
            InputStream actionInputStream = AAPCConfigManager.class.getClassLoader().getResourceAsStream(actionConfigName);
            actionActProperties = new Properties();
            actionActProperties.load(actionInputStream);

            String parameterConfigName = applicationProperties.getProperty("parameter.config.file");
            InputStream paramInputStream = AAPCConfigManager.class.getClassLoader().getResourceAsStream(parameterConfigName);
            paramCalProperties = new Properties();
            paramCalProperties.load(paramInputStream);

            String robotConfigName = applicationProperties.getProperty("robot.config.file");
            InputStream robotInputStream = AAPCConfigManager.class.getClassLoader().getResourceAsStream(robotConfigName);
            robotProperties = new Properties();
            robotProperties.load(robotInputStream);
        } catch (IOException e) {
            //e.printStackTrace();
            throw new ExecutionLogicException(e.getMessage());
        }
    }
}
