package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2022/9/19.
 *
 * @author Chen Gao
 */
@RestController
@RequestMapping("/api/v1/article/search")
public class ArticleSearchController {
    @Autowired
    private ArticleSearchService searchService;
    /**
     * 文章搜索功能
     */
    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto searchDto){
        return searchService.search(searchDto);
    }
}
