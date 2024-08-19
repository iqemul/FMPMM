package edu.hit.fmpmm.controller;

import co.nstant.in.cbor.CborException;
import edu.hit.fmpmm.domain.neo4j.node.Node;
import edu.hit.fmpmm.dto.Result;
import edu.hit.fmpmm.service.process.ProcessService;
import edu.hit.fmpmm.service.process.UserReqService;
import edu.hit.fmpmm.service.web.UserMsgService;
import edu.hit.fmpmm.service.web.UserService;
import edu.hit.fmpmm.util.AsyncTask;
import edu.hit.fmpmm.util.MsgHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
//@CrossOrigin(origins = "http://10.245.1.6:5173/")
@RequestMapping("/sim")
public class SimController {
    private final ProcessService processService;
    private final UserReqService userReqService;
    private final UserService userService;
    private final AsyncTask asyncTask;
    private final UserMsgService userMsgService;
    private final MsgHandler msgHandler;

    @Autowired
    public SimController(
            ProcessService processService, UserReqService userReqService, UserService userService, AsyncTask asyncTask,
            UserMsgService userMsgService, MsgHandler msgHandler) {
        this.processService = processService;
        this.userReqService = userReqService;
        this.userService = userService;
        this.asyncTask = asyncTask;
        this.userMsgService = userMsgService;
        this.msgHandler = msgHandler;
    }

    @PostMapping("sheet")
    public String process(@RequestParam("file") MultipartFile file) {  // 工艺单 TODO 返回啥
        if (file.isEmpty()) {
            return "上传的工艺单为空";
        }
        return "lmq";
    }

    /**
     * 根据用户输入进行轨迹规划
     *
     * @param obj 成品
     * @return 运动轨迹
     */
    @GetMapping("/product")
    public Result product(@RequestParam("obj") String obj) {
        List<Map<String, List<Node>>> processes = userReqService.requirementAnalyse(obj);
        if (processes == null || processes.isEmpty()) {
            return Result.fail("无法推理出要执行的工艺 T_T");
        }
        CompletableFuture<Result> resultCompletableFuture = asyncTask.supplyAsync(processes, this.processService::handleProcess);
        userMsgService.saveUserMsgFromAsyncTask(resultCompletableFuture, this.msgHandler::processExecMsgHandler);  // 存入数据库
        return Result.success("马上进行工艺路径规划 (❁´◡`❁)");
    }

    @GetMapping("reasoning_process")
    public Result reasoning(@RequestParam("obj1") String obj1, @RequestParam("obj2") String obj2) {
        // 分析过程可能较慢，使用异步任务
        Map<String, String> objs = new HashMap<>();
        objs.put("obj1", obj1);
        objs.put("obj2", obj2);
        CompletableFuture<Result> resultCompletableFuture = asyncTask.supplyAsync(objs, this.userReqService::processRec);
        userMsgService.saveUserMsgFromAsyncTask(resultCompletableFuture, this.msgHandler::processRecMsgHandler);
        /*ReasoningResult result = userReqService.reasoningAnalyse(obj1, obj2);
        List<Goal> goalsLikely = result.getGoals();
        List<List<Instance>> robots4obj1 = result.getRobots4obj1();
        List<List<Instance>> robots4obj2 = result.getRobots4obj2();*/
        return Result.success("马上进行工艺推荐 ...( ＿ ＿)ノ｜");
    }

    /**
     * 设置场景文件
     *
     * @return 设置并加载场景文件是否成功
     */
    @PostMapping("set_scene")
    public Result setSceneFile(@RequestParam("file") MultipartFile file) {  //
        Result uploadResult = userService.uploadSceneFile(file);
        if (uploadResult.getSuccess()) {
            try {
                processService.loadSceneFile(((Path) (uploadResult.getData())).toString());
            } catch (CborException e) {
                // throw new RuntimeException(e);
                return Result.fail("上传场景文件成功，加载场景文件失败");
            }
        } else {
            return uploadResult;
        }
        return Result.success("加载场景文件成功");
    }
}
