package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

/**
 * Created on 2022/9/22.
 *
 * @author Chen Gao
 */
public interface ApAssociateWordsService {

    /**
     * 搜索联想词查询
     * @param dto
     * @return
     */
    public ResponseResult search(UserSearchDto dto);

}
