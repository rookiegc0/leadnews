package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.entity.WmNews;

/**
 * 自媒体图文内容信息表(WmNews)表服务接口
 *
 * @author makejava
 * @since 2022-09-09 11:45:52
 */
public interface WmNewsService extends IService<WmNews> {

    /**
     * 分页查询文章数据
     * @param pageReqDto
     * @return
     */
    ResponseResult listNews(WmNewsPageReqDto pageReqDto);

    /**
     * 自媒体端提交文章
     * @param newsDto
     * @return
     */
    ResponseResult submit(WmNewsDto newsDto);

    /**
     * 文章上下架
     * @param newsDto
     * @return
     */
    ResponseResult downOrUp(WmNewsDto newsDto);
}

