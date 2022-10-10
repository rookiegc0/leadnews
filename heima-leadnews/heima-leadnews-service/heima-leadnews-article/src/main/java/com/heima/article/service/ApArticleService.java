package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.entity.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.mess.ArticleVisitStreamMess;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章表(ApArticle)表服务接口
 *
 * @author makejava
 * @since 2022-09-07 23:48:37
 */
public interface ApArticleService extends IService<ApArticle> {
    /**
     * 根据加载动态实现动态查询，很重要
     * @param articleHomeDto
     * @param loadType
     * @return
     */
    List<HotArticleVo> loadArticle(ArticleHomeDto articleHomeDto, Integer loadType);

    /**
     * 根据加载动态实现动态查询，很重要
     * @param articleHomeDto
     * @param loadType
     * @return
     */
    List<HotArticleVo> loadArticle2(ArticleHomeDto articleHomeDto, Integer loadType, boolean firstPage);

    /**
     * 自媒体端文章审核之后保存app端文章相关
     * @param articleDto
     * @return
     */
    ResponseResult saveArticle(ArticleDto articleDto);

    /**
     * 更新文章的分值  同时更新缓存中的热点文章数据
     * @param mess
     */
    public void updateScore(ArticleVisitStreamMess mess);
}

