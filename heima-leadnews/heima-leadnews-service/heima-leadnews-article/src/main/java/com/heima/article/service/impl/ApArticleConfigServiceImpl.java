package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.model.article.entity.ApArticleConfig;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 文章配置表(ApArticleConfig)表服务实现类
 *
 * @author makejava
 * @since 2022-09-07 23:48:37
 */
@Service("apArticleConfigService")
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {

    @Override
    public void updateByMap(Map map) {

        Object enable = map.get("enable");
        boolean isDown = true;
        if (enable.equals(1)){
            isDown = false;
        }
        //修改文章
        update(Wrappers.<ApArticleConfig>lambdaUpdate().eq(ApArticleConfig::getArticleId, map.get("articleId"))
                .set(ApArticleConfig::getDown, isDown));

    }
}

