package edu.hit.fmpmm.controller;

import edu.hit.fmpmm.dto.Result;
import edu.hit.fmpmm.service.web.UserMsgService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/msg")
public class UserMsgController {
    private final UserMsgService userMsgService;

    public UserMsgController(UserMsgService userMsgService) {
        this.userMsgService = userMsgService;
    }

    @GetMapping("/get")
    public Result getUserMsg() {
        return userMsgService.getUserMsg();
    }

    @PostMapping("update2read")
    public Result update2read(@RequestBody String msgId) {
        return userMsgService.update2read(Integer.parseInt(msgId));
    }

    @DeleteMapping("delete_msg")
    public Result deleteMsg(@RequestBody String msgId) {
        return userMsgService.deleteMsg(Integer.parseInt(msgId));
    }
}
