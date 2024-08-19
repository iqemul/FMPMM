package edu.hit.fmpmm.service.aapc;

import java.util.Objects;

public interface APFactory {

    /**
     * 根据配置文件中的信息得到需要创建的类
     *
     * @param type 是动作执行器的工厂还是参数计算器的工厂在调用
     * @param key  动作类型 | 参数类型 | 机器人型号 配置文件中的键（和生产对象的类别相关的信息）
     * @return 生产对象的全类名
     */
    default String getProperty(String type, String key) {
        if (Objects.equals(type, "action")) {
            return AAPCConfigManager.AA_PC_CONFIG.getActionActProperties().getProperty(key);
        } else if (Objects.equals(type, "parameter")) {
            return AAPCConfigManager.AA_PC_CONFIG.getParamCalProperties().getProperty(key);
        } else if (Objects.equals(type, "robot")) {
            return AAPCConfigManager.AA_PC_CONFIG.getRobotProperties().getProperty(key);
        } else {
            return "";
        }
    }
}
