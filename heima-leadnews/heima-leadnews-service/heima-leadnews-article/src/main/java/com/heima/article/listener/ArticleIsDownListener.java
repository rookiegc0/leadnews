package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.WmNewsMessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created on 2022/9/17.
 *
 * @author Chen Gao
 */
@Component
@Slf4j
public class ArticleIsDownListener {

    @Autowired
    private ApArticleConfigService articleConfigService;

    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void onMessage(String message){
        if (StringUtils.isNotBlank(message)){
            Map map = JSON.parseObject(message, Map.class);
            articleConfigService.updateByMap(map);
        }
    }

}
