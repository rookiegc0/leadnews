package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.entity.WmChannel;

/**
 * 频道信息表(WmChannel)表服务接口
 *
 * @author makejava
 * @since 2022-09-09 11:45:51
 */
public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有数据
     * @return
     */
    ResponseResult findAll();
}

