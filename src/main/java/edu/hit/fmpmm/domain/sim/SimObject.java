package edu.hit.fmpmm.domain.sim;

import co.nstant.in.cbor.CborException;
import edu.hit.fmpmm.domain.exception.ExecutionLogicException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 仿真中的对象，包含的属性是每个对象都具有的
 */
@Data
public class SimObject {
    private SimClient client;  // 客户端

    private String path;  // 在仿真中的路径

    private int handle;
    private List<Double> pose = new ArrayList<>();

    private List<Double> position = new ArrayList<>();

    private List<Double> orientation = new ArrayList<>();
    private List<Double> quaternion = new ArrayList<>();

    public SimObject() {
    }

    public SimObject(SimClient client, String path) {
        this.client = client;
        this.path = path;
        this.handle = this.getObjectHandle(path);
    }

    protected int getObjectHandle(String path) {  // 得到path代表的这个对象的handle
        if (client == null) {
            return -1;
        }
        int handle = -1;
        if (path != null && !path.isEmpty()) {
            try {
                handle = Math.toIntExact(this.client.getSim().getObject(path));
            } catch (CborException | RuntimeException e) {
                // e.printStackTrace();
                throw new ExecutionLogicException("在场景中无法检索到" + path);
            }
        }
        return handle;
    }

    public List<Double> getPose() {

        try {
            return this.getClient().getSim().getObjectPose(this.getHandle(), -1);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Double> getPosition() {
        try {
            return this.getClient().getSim().getObjectPosition(this.getHandle(), -1);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Double> getOrientation() {
        try {
            return this.getClient().getSim().getObjectOrientation(this.getHandle(), -1);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Double> getQuaternion() {
        try {
            return getClient().getSim().getObjectQuaternion(handle, -1);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPosition(List<Double> position) {
        try {
            client.getSim().setObjectPosition(handle, position, -1);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOrientation(List<Double> orientation) {
        try {
            client.getSim().setObjectOrientation(handle, orientation, -1);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    public void setQuaternion(List<Double> quaternion) {
        try {
            client.getSim().setObjectQuaternion(handle, quaternion, -1);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPose(List<Double> pose) {
        try {
            client.getSim().setObjectPose(handle, pose, -1);
        } catch (CborException e) {
            throw new RuntimeException(e);
        }
    }

    public void setClient(SimClient client) {
        this.client = client;
        if (path != null && !path.isEmpty() && handle == -1) {
            handle = this.getObjectHandle(path);
        }
    }

    public void setPath(String path) {
        this.path = path;
        if (client != null && handle == -1) {
            handle = this.getObjectHandle(path);
        }
    }
}
