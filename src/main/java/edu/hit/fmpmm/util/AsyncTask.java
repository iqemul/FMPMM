package edu.hit.fmpmm.util;

import edu.hit.fmpmm.dto.Result;
import edu.hit.fmpmm.util.cache.CacheClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class AsyncTask {
    @Qualifier("taskExecutor")
    private final Executor executor;
    private final CacheClient cacheClient;

    @Autowired
    public AsyncTask(Executor executor, CacheClient cacheClient) {
        this.executor = executor;
        this.cacheClient = cacheClient;
    }

    /**
     * 异步执行一个任务，并将任务执行的结果保存到缓存中
     *
     * @param param 异步任务task需要传入的参数
     * @param task  异步任务
     * @param <T>   task需要参数的类型
     */
    public <T> void supplyAsyncAndSave2Cache(String key, Long time, TimeUnit timeUnit, T param, Function<T, Result> task) {
        CompletableFuture<Result> supplyAsync = supplyAsync(param, task);
        supplyAsync.thenAccept(result -> {
            System.out.println("result:" + result);
            cacheClient.setHash(key, result, time, timeUnit);
        });
    }

    public <T> CompletableFuture<Result> supplyAsync(T param, Function<T, Result> task) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            try {
                return task.apply(param);
            } catch (Exception e) {
                return Result.fail(e.getMessage());
            }
        }, executor);
    }
}
