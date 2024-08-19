package edu.hit.fmpmm.service.aapc.aa.actions;

import co.nstant.in.cbor.CborException;
import edu.hit.fmpmm.domain.sim.robot.Robot;
import edu.hit.fmpmm.service.aapc.aa.ActionActuator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 夹爪旋转，不改变夹爪的张开闭合状态
 */
@Component
public class Rotate extends ActionActuator {

    public Rotate() {
    }

    public Rotate(Robot robot) {
        super(robot);
    }

    @Override
    public boolean go(Map<String, Object> params) throws CborException {
        boolean flag;
        commonSettings(params);
        // 需要的参数：角度，顺逆时针
        Object tmpTheta = params.get("rotate_theta");
        Object tmpOri = params.get("rotate_ori");
        if (tmpTheta == null || tmpOri == null) {
            flag = false;
        } else {
            double theta = (double) tmpTheta;
            char ori = (char) tmpOri;
            // 不同机器人可能有不同的旋转方式, ori
            if (ori == '-') {
                theta = -theta;
            }
            flag = this.robot.rotate(theta);
        }

        return flag;
    }
}
