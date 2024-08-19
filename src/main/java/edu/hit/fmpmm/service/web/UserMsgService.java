package edu.hit.fmpmm.service.web;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.hit.fmpmm.domain.web.UserMsg;
import edu.hit.fmpmm.dto.Result;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface UserMsgService extends IService<UserMsg> {

    void saveUserMsgFromAsyncTask(CompletableFuture<Result> supplyAsync, Function<Result, UserMsg> function);

    Result getUserMsg();

    Result update2read(Integer msgId);

    Result deleteMsg(int msgId);

    boolean deleteAllMsgByUser(int userId);
}
