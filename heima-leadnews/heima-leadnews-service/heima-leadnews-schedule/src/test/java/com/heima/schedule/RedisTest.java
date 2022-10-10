package com.heima.schedule;

import com.heima.common.redis.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created on 2022/9/15.
 *
 * @author Chen Gao
 */
@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private CacheService cacheService;

    @Test
    public void testList(){

        //1。在list左边添加元素
        cacheService.lLeftPush("list_001", "hello_redis");
        //2.在list右边获取元素，并且删除

    }

}
