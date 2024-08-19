package edu.hit.fmpmm.service.web.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hit.fmpmm.domain.web.UserMsg;
import edu.hit.fmpmm.dto.Result;
import edu.hit.fmpmm.dto.UserDTO;
import edu.hit.fmpmm.mapper.UserMsgMapper;
import edu.hit.fmpmm.server.WebSocketServer;
import edu.hit.fmpmm.service.web.UserMsgService;
import edu.hit.fmpmm.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
public class UserMsgServiceImpl extends ServiceImpl<UserMsgMapper, UserMsg> implements UserMsgService {
    private final UserMsgMapper userMsgMapper;

    @Autowired
    public UserMsgServiceImpl(UserMsgMapper userMsgMapper) {
        this.userMsgMapper = userMsgMapper;
    }

    /**
     * 将异步任务的结果保存到数据库表
     *
     * @param supplyAsync 异步任务
     */
    @Override
    public void saveUserMsgFromAsyncTask(CompletableFuture<Result> supplyAsync, Function<Result, UserMsg> function) {
        UserDTO user = UserHolder.getUser();
        supplyAsync.thenAccept(result -> {
            if (user == null) {
                return;
            }
            UserMsg userMsg = function.apply(result);
            userMsg.setUserId(user.getId());
            userMsg.setSendTime(new Timestamp(System.currentTimeMillis()));
            userMsg.setStatus(0);
            save(userMsg);
            // 向这个用户的客户端发送消息
            try {
                WebSocketServer.sendMessage(userMsg.getSignal(), String.valueOf(userMsg.getUserId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Result getUserMsg() {
        QueryWrapper<UserMsg> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserHolder.getUser().getId());
        List<UserMsg> userMsgList = list(queryWrapper);
        return Result.success(userMsgList);
    }

    @Override
    public Result update2read(Integer msgId) {
        UserMsg msg = query().eq("id", msgId).one();
        if (msg == null) {
            return Result.fail("标记为已读失败");
        }
        msg.setStatus(1);
        boolean updated = updateById(msg);
        if (updated) {
            return Result.success("成功已读");
        }
        return Result.fail("标记为已读失败");
    }

    @Override
    public Result deleteMsg(int msgId) {
        int cnt = userMsgMapper.deleteById(msgId);
        if (cnt > 0) {
            return Result.success();
        }
        return Result.fail("删除消息失败");
    }

    @Override
    public boolean deleteAllMsgByUser(int userId) {
        QueryWrapper<UserMsg> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserMsg> msgList = list(queryWrapper);
        if (msgList.isEmpty()) {
            return true;
        }
        return remove(queryWrapper);
    }
}
