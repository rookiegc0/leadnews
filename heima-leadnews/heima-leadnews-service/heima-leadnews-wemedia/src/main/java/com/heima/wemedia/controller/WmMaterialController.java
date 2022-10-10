package com.heima.wemedia.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;

import java.io.IOException;
import java.util.List;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.entity.WmMaterial;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 自媒体图文素材信息表(WmMaterial)表控制层
 *
 * @author makejava
 * @since 2022-09-09 11:45:51
 */
@Api(tags = "自媒体图文素材信息表接口")
@RestController
@RequestMapping("/api/v1/material")
@Slf4j
public class WmMaterialController {
    /**
     * Service层对象
     */
    @Autowired
    private WmMaterialService wmMaterialService;

    /**
     * 需求：图片上传
     */
    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        WmMaterial material = null;
        try {
            material = wmMaterialService.uploadPic(multipartFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (material == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "上传文件为空");
        }
        return ResponseResult.okResult(material);
    }

    /**
     * 媒体的分页查询
     * 参数：{"page":1,"size":20,"isCollection":0}
     */
    @PostMapping("/list")
    public PageResponseResult list(@RequestBody WmMaterialDto wmMaterialDto){
        return wmMaterialService.pageMaterial(wmMaterialDto);
    }

    /**
     * 分页查询所有数据
     *
     * @param pageRequest 分页对象
     * @param wmMaterial  查询实体
     * @return 所有数据
     */
    @ApiOperation("分页查询所有数据")
    @GetMapping
    public ResponseEntity<Page<WmMaterial>> paginQuery(WmMaterial wmMaterial, PageRequest pageRequest) {
        //1.分页参数
        long current = pageRequest.getPageNumber();
        long pageSize = pageRequest.getPageSize();

        //2.构造分页构造器
        Page pageInfo = new Page(current, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<WmMaterial> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件

        //3.执行查询
        wmMaterialService.page(pageInfo, queryWrapper);
        return ResponseEntity.ok(pageInfo);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation("通过主键查询单条数据")
    @GetMapping("{id}")
    public ResponseEntity<WmMaterial> selectOne(@PathVariable Serializable id) {
        return ResponseEntity.ok(wmMaterialService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param wmMaterial 实体对象
     * @return 新增结果
     */
    @ApiOperation("新增数据")
    @PostMapping
    public ResponseEntity<String> add(@RequestBody WmMaterial wmMaterial) {
        wmMaterialService.save(wmMaterial);
        return ResponseEntity.ok("新增成功！");
    }

    /**
     * 修改数据
     *
     * @param wmMaterial 实体对象
     * @return 修改结果
     */
    @ApiOperation("修改数据")
    @PutMapping
    public ResponseEntity<String> edit(@RequestBody WmMaterial wmMaterial) {
        wmMaterialService.updateById(wmMaterial);
        return ResponseEntity.ok("修改成功！");
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 删除结果
     */
    @ApiOperation("通过主键删除数据")
    @DeleteMapping
    public ResponseEntity<String> deleteById(@RequestParam Integer id) {
        wmMaterialService.removeById(id);
        return ResponseEntity.ok("删除成功！");
    }
}

