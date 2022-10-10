package com.heima.schedule.feign;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created on 2022/9/16.
 *
 * @author Chen Gao
 */
@RestController
public class ScheduleClient implements IScheduleClient {

    @Autowired
    private TaskService taskService;

    @PostMapping("/api/v1/task/add")
    @Override
    public ResponseResult addTask(@RequestBody Task task) {
        return ResponseResult.okResult(taskService.addTask(task));
    }

    @GetMapping("/api/v1/task/{taskId}")
    @Override
    public ResponseResult cancelTask(@PathVariable("taskId") Long taskId) {
        return ResponseResult.okResult(taskService.cancelTask(taskId));
    }

    @GetMapping("/api/v1/task/{type}/{priority}")
    @Override
    public ResponseResult poll(@PathVariable("type")int type,@PathVariable("priority")int priority) {
        return ResponseResult.okResult(taskService.poll(type,priority));
    }
}
