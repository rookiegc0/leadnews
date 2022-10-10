package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;

/**
 * Created on 2022/9/21.
 *
 * @author Chen Gao
 */
public interface ApUserSearchService {

    /**
     * 保存用户搜索记录历史记录
     */
    public void insert(String keyword,Integer userId);

    /**
     * 查询用户搜索记录的历史记录
     * @return
     */
    public ResponseResult findUserSearch();

    /**
     * 删除历史记录
     */
    public ResponseResult delUserSearch(HistorySearchDto dto);
}
