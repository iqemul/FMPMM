package edu.hit.fmpmm;

import edu.hit.fmpmm.service.web.impl.UserMsgServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FmpmmApplicationTests {
    @Autowired
    UserMsgServiceImpl userMsgService;

    @Test
    public void test() {

    }

}
