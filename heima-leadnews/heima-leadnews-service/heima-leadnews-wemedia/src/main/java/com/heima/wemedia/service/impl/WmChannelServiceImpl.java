package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.entity.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.stereotype.Service;

/**
 * 频道信息表(WmChannel)表服务实现类
 *
 * @author makejava
 * @since 2022-09-09 11:45:51
 */
@Service("wmChannelService")
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {

    /**
     * 查询所有数据
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return null;
    }
}

