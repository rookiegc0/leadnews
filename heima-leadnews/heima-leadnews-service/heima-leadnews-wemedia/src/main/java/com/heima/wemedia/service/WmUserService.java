package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.entity.WmUser;

/**
 * 自媒体用户信息表(WmUser)表服务接口
 *
 * @author makejava
 * @since 2022-09-09 11:45:52
 */
public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体端登录
     * @param dto
     * @return
     */
    ResponseResult login(WmLoginDto dto);
}

