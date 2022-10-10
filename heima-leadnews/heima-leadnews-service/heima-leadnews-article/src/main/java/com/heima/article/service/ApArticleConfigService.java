package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.entity.ApArticleConfig;

import java.util.Map;

/**
 * 文章配置表(ApArticleConfig)表服务接口
 *
 * @author makejava
 * @since 2022-09-07 23:48:37
 */
public interface ApArticleConfigService extends IService<ApArticleConfig> {

    /**
     * 上下架以后修改文章config
     * @param map
     */
    void updateByMap(Map map);
}

