package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.dtos.Taskinfo;
import com.heima.model.schedule.dtos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.decorators.ScheduledCache;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.util.resources.cldr.aa.CalendarData_aa_ER;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created on 2022/9/15.
 *
 * @author Chen Gao
 */
@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    /**
     * 添加延迟任务
     * @param task
     * @return
     */
    @Override
    public long addTask(Task task) {
        //1.添加任务到数据库中
        boolean success = addTaskToDb(task);

        if (success){
            //2.添加任务到redis
            addTaskToCache(task);
        }
        return task.getTaskId();
    }

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(Long taskId) {

        boolean flag = false;

        //删除任务，更新任务日志
        Task task = updateDb(taskId,ScheduleConstants.CANCELLED);//这里第二个参数目的是为了做成一个通用方法，方便别人做取消用

        //删除redis数据
        if (task != null){
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    /**
     * 拉取任务
     * @param type
     * @param priority
     * @return
     */
    @Override
    public Task poll(int type, int priority) {

        String key = type+"_"+priority;
        Task task = null;

        try {
            //1.从redis拉去数据
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(task_json)){
                task = JSON.parseObject(task_json,Task.class);

                //2.修改数据库信息
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            log.error("任务拉取异常");
        }

        return task;
    }

    /**
     * 删除redis数据
     * @param task
     */
    private void removeTaskFromCache(Task task) {

        String key = task.getTaskType()+"_"+task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()){
            cacheService.lRemove(ScheduleConstants.TOPIC+key, 0, JSON.toJSONString(task));
        }else {
            cacheService.zRemove(ScheduleConstants.FUTURE+key, JSON.toJSONString(task));
        }
    }

    /**
     * 删除任务，更新任务数据
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDb(Long taskId, int status) {
        Task task = null;
        try {
            //删除任务
            taskinfoMapper.deleteById(taskId);
            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (BeansException e) {
            log.error("任务取消异常 taskId={}",taskId);
        }

        return task;
    }

    @Autowired
    private CacheService cacheService;
    /**
     * 添加任务到redis中
     * @param task
     */
    private void addTaskToCache(Task task) {

        String key = task.getTaskType()+"_"+task.getPriority();

        //搞一个预设时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();

        //2.1如果任务执行时间小于当前时间，存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key, JSON.toJSONString(task));
        }else if((task.getExecuteTime() <= (System.currentTimeMillis() + nextScheduleTime))){
            //2.2如果任务执行时间大于等于当前时间，小于等于预设时间（如未来五分钟）。存入zset
            cacheService.zAdd(ScheduleConstants.FUTURE+key, JSON.toJSONString(task), task.getExecuteTime());
        }



    }

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    /**
     * 添加任务到数据库中
     * @param task
     * @return
     */
    private boolean addTaskToDb(Task task) {

        boolean flag = false;

        try {
            //保存任务表
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));//这个字段的类型不一样，所以需要单独设置一下
            taskinfoMapper.insert(taskinfo);

            //在这里需要设置taskid,开始的那个方法是需要返回值的，所以需要找个地方把值弄出来返回给他
            task.setTaskId(taskinfo.getTaskId());
            //保存任务日志数据
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
            flag = true;
        } catch (BeansException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 未来数据队列定时刷新，和下面不一样，下面是数据库定时刷新的东西，要分开来看
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){

        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);

        if (StringUtils.isNotBlank(token)){
            log.info("未来数据定时刷新---定时任务");

            //获取所有未来数据的集合key
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {

                //获取当前数据的key topic
                String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
                //按照key和分值查询符合条件的数据
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

                //同步数据（主要使用管道技术）
                if (!tasks.isEmpty()){
                    cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                    log.info("成功将"+futureKey+"刷新到了"+topicKey);
                }
            }
        }


    }

    /**
     * 数据库任务定时同步到redis
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData(){

        //清理缓存中的数据 list zset
        clearCache();
        //查询符合条件的任务 小于未来五分钟的数据
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        List<Taskinfo> taskinfoList = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery()
                .lt(Taskinfo::getExecuteTime, calendar.getTime()));
        //把任务添加到redis
        if (taskinfoList != null && taskinfoList.size() > 0){
            for (Taskinfo taskinfo : taskinfoList) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }
        log.info("数据库的task存入到了redis");
    }

    /**
     * 清楚缓存中的数据的方法
     */
    public void clearCache(){
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);
    }

}
