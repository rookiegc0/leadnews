package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.entity.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 自媒体图文素材信息表(WmMaterial)表服务接口
 *
 * @author makejava
 * @since 2022-09-09 11:45:51
 */
public interface WmMaterialService extends IService<WmMaterial> {

    WmMaterial uploadPic(MultipartFile multipartFile) throws IOException;

    /**
     * 根据过滤条件分页查询
     * @param wmMaterialDto
     * @return
     */
    PageResponseResult pageMaterial(WmMaterialDto wmMaterialDto);
}

