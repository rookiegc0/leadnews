package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.entity.ApArticle;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 文章表(ApArticle)表数据库访问层
 *
 * @author makejava
 * @since 2022-09-07 23:48:37
 */
public interface ApArticleMapper extends BaseMapper<ApArticle> {


    /**
     * 加载文章列表
     * @param articleHomeDto
     * @param loadType
     * @return
     */
    List<ApArticle> selectListWith(@Param("articleHomeDto") ArticleHomeDto articleHomeDto, @Param("loadType") Integer loadType);

    public List<ApArticle> findArticleListByLast5days(@Param("dayParam") Date dayParam);
}

