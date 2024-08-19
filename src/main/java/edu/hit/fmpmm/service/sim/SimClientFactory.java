package edu.hit.fmpmm.service.sim;

import com.coppeliarobotics.remoteapi.zmq.RemoteAPIClient;
import edu.hit.fmpmm.config.CoppeliaSimConfig;
import edu.hit.fmpmm.domain.sim.SimClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQException;

@Data
@Component
public class SimClientFactory {

    private final CoppeliaSimConfig simConfig;
    private String scene;

    @Autowired
    public SimClientFactory(CoppeliaSimConfig simConfig) {
        this.simConfig = simConfig;
    }

    public SimClient createSimClient() {
        RemoteAPIClient client = null;
        try {
            client = new RemoteAPIClient(
                    simConfig.getHost(), simConfig.getPort()
            );
        } catch (ZMQException e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
            // TODO 打开软件
        }
        if (client != null) {
            if (scene != null) {
                return new SimClient(client, scene);
            } else {
                return new SimClient(client);
            }
        }
        throw new RuntimeException("连接CoppeliaSim失败");
    }
}
