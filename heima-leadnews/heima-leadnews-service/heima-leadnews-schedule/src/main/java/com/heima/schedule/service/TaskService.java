package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

/**
 * Created on 2022/9/15.
 *
 * @author Chen Gao
 */
public interface TaskService {

    /**
     * 添加延迟任务
     */
    public long addTask(Task task);

    /**
     * 取消任务
     */
    public boolean cancelTask(Long taskId);

    /**
     * 按照类型和优先度消费任务
     *
     * @param type
     * @param priority
     * @return
     */
    public Task poll(int type, int priority);
}
