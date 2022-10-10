package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.entity.WmMaterial;
import com.heima.utils.threads.ThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * 自媒体图文素材信息表(WmMaterial)表服务实现类
 *
 * @author makejava
 * @since 2022-09-09 11:45:51
 */
@Service("wmMaterialService")
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    FileStorageService fileStorageService;

    @Override
    public WmMaterial uploadPic(MultipartFile multipartFile) throws IOException {
        //1.接收上传文件：使用minio客户端上传到服务器，返回url
        if (multipartFile == null || multipartFile.getSize() == 0L){
            return null;
        }
        //获取文件名称
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString().replace("-", "");
        String url = fileStorageService.uploadImgFile("", filename + suffix, multipartFile.getInputStream());
        //2.保存上传的媒体记录：构建媒体对象插入到表中
        WmMaterial material = new WmMaterial();
        material.setUserId(ThreadLocalUtils.getFromThreadLocal().getId());
        material.setUrl(url);
        material.setType(0);
        material.setIsCollection(0);
        material.setCreatedTime(new Date());
        this.save(material);
        return material;
    }

    @Override
    public PageResponseResult pageMaterial(WmMaterialDto wmMaterialDto) {
        //1.调用page()方法完成分页查询
        Integer currentPage = wmMaterialDto.getPage();
        Integer pagesize = wmMaterialDto.getSize();
        Short isCollection = wmMaterialDto.getIsCollection();
        LambdaQueryWrapper<WmMaterial> lqw = new LambdaQueryWrapper<>();
        lqw.eq(WmMaterial::getUserId,ThreadLocalUtils.getFromThreadLocal().getId());
        lqw.eq(isCollection != null,WmMaterial::getIsCollection,isCollection);
        Page<WmMaterial> page = this.page(new Page<WmMaterial>(currentPage, pagesize), lqw);
        //2.构建pageresponseresult
        PageResponseResult pageResponseResult = new PageResponseResult(currentPage, pagesize, (int) page.getTotal());
        ResponseResult responseResult = ResponseResult.okResult(page.getRecords());
        BeanUtils.copyProperties(responseResult, pageResponseResult);

        return pageResponseResult;
    }
}

