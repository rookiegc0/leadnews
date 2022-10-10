package com.heima.model.article.vos;

import com.heima.model.article.entity.ApArticle;
import lombok.Data;

/**
 * Created on 2022/9/23.
 *
 * @author Chen Gao
 */
@Data
public class HotArticleVo extends ApArticle {

    /**
     * 文章分值
     */
    private Long score;
}
