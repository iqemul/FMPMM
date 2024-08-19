package edu.hit.fmpmm.domain.sim;

import com.coppeliarobotics.remoteapi.zmq.RemoteAPIClient;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import lombok.Data;

@Data
public class SimClient {
    private RemoteAPIClient client;
    private String scene;

    // 要把sim和client绑定在一起的原因是使用同一个客户端的对象要使用同一个sim
    private RemoteAPIObjects._sim sim;
    private RemoteAPIObjects._simIK simIK;
    private RemoteAPIObjects._simOMPL simOMPL;

    private boolean callbackFlag = false;

    public SimClient(RemoteAPIClient client) {
        this.client = client;
        this.sim = client.getObject().sim();
        this.simIK = client.getObject().simIK();
        this.simOMPL = client.getObject().simOMPL();
    }

    public SimClient(RemoteAPIClient client, String scene) {
        this(client);
        this.scene = scene;
    }
}
