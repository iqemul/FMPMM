package edu.hit.fmpmm.domain.sim;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Dummy extends SimObject {  // 表示仿真中dummy类型的对象

    public Dummy() {
    }

    public Dummy(SimClient client, String path) {
        super(client, path);
    }

}
