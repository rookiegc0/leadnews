package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

/**
 * Created on 2022/9/19.
 *
 * @author Chen Gao
 */
public interface ArticleSearchService {

    /**
     * 根据条件搜索
     * @param searchDto
     * @return
     */
    ResponseResult search(UserSearchDto searchDto);
}
