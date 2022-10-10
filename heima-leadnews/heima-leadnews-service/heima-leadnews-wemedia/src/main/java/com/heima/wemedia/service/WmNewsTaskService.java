package com.heima.wemedia.service;

import javax.xml.crypto.Data;
import java.util.Date;

/**
 * Created on 2022/9/16.
 *
 * @author Chen Gao
 */
public interface WmNewsTaskService {

    /**
     * 添加任务到延迟队列中
     */
    public void addNewsToTask(Integer id, Date publishTime);

    /**
     * 消费任务，审核文章
     */
    public void scanNewsByTask();
}
