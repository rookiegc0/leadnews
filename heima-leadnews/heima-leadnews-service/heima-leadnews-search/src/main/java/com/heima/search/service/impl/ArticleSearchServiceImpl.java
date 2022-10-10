package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.ArticleSearchService;
import com.heima.utils.threads.AppThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/9/19.
 *
 * @author Chen Gao
 */
@Service
@Slf4j
public class ArticleSearchServiceImpl implements ArticleSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ApUserSearchService apUserSearchService;

    /**
     * 根据条件搜索
     *
     * @param searchDto
     * @return
     */
    @Async
    @Override
    public ResponseResult search(UserSearchDto searchDto) {
        //1.创建搜索请求对象，并且指定索引库
        SearchRequest searchRequest = new SearchRequest("app_info_article");
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //关键字查询
        if (searchDto.getSearchWords() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchDto.getSearchWords()));
        }

        Integer id = AppThreadLocalUtils.getFromThreadLocal().getId();

        //异步调用，保存搜索记录
        if (id != null && searchDto.getFromIndex() == 0){
            apUserSearchService.insert(searchDto.getSearchWords(), AppThreadLocalUtils.getFromThreadLocal().getId());
        }
        //小于最小时间
        if (searchDto.getMinBehotTime() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("publishTime").lte(searchDto.getMinBehotTime()));
        }

        searchRequest.source().query(boolQueryBuilder);

        //分页
        searchRequest.source().from(0).size(searchDto.getPageSize());

        //排序
        searchRequest.source().sort("publishTime", SortOrder.DESC);

        //高亮
        HighlightBuilder highlight = new HighlightBuilder();
        highlight.field("title");
        highlight.requireFieldMatch(false);
        highlight.preTags("<font style= 'color: red; font-size: inherit;'>");
        highlight.postTags("</font>");
        searchRequest.source().highlighter(highlight);

        //调用es的搜索api
        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //结果封装
        return result(response);
    }

    /**
     * 抽取的封装结果的方法
     *
     * @param response
     * @return
     */
    private ResponseResult result(SearchResponse response) {

        List<Map> list = new ArrayList<>();

        //获取命中的结果
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            //获取查询结果，返回是map集合
            Map<String, Object> map = hit.getSourceAsMap();
            //高亮数据封装
            //如果高亮域中有值
            if (hit.getHighlightFields().size() > 0) {
                //提取出高亮域
                Text[] titles = hit.getHighlightFields().get("title").fragments();
                if (titles.length > 0) {
                    //把高亮之后的h_title=<font>测试</font>
                    map.put("h_title", titles[0].toString());
                } else {
                    //表示没有高亮区域，我们则把h_title=测试
                    map.put("h_title", map.get("title"));
                }
                //把获取的结果封装到list
                list.add(map);
            }
        }
        return ResponseResult.okResult(list);
    }


}
