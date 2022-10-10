package com.heima.common.constants;

/**
 * Created on 2022/9/8.
 *
 * @author Chen Gao
 */
public interface LoadArticleType {

    /**
     * 上拉或下拉加载更多
     */
    public static final Integer LOAD_MORE = 1;

    /**
     * 加载最新
     */
    public static final Integer LOAD_NEW = 2;
    public static final String DEFAULT_TAG = "__all__";
    public static final Integer HOT_ARTICLE_LIKE_WEIGHT = 3;
    public static final Integer HOT_ARTICLE_COMMENT_WEIGHT = 5;
    public static final Integer HOT_ARTICLE_COLLECTION_WEIGHT = 8;

    public static final String HOT_ARTICLE_FIRST_PAGE = "hot_article_first_page_";
}
