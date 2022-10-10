package com.heima.wemedia.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;

import java.util.List;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.entity.WmUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.wemedia.service.WmUserService;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 自媒体用户信息表(WmUser)表控制层
 *
 * @author makejava
 * @since 2022-09-09 11:45:52
 */
@Api(tags = "自媒体用户信息表接口")
@RestController
@RequestMapping("/api/v1/wmUser")
@Slf4j
public class WmUserController {
    /**
     * Service层对象
     */
    @Autowired
    private WmUserService wmUserService;

    @PostMapping("/in")
    public ResponseResult login(@RequestBody WmLoginDto dto){
        return wmUserService.login(dto);
    }

    /**
     * 分页查询所有数据
     *
     * @param pageRequest 分页对象
     * @param wmUser      查询实体
     * @return 所有数据
     */
    @ApiOperation("分页查询所有数据")
    @GetMapping
    public ResponseEntity<Page<WmUser>> paginQuery(WmUser wmUser, PageRequest pageRequest) {
        //1.分页参数
        long current = pageRequest.getPageNumber();
        long pageSize = pageRequest.getPageSize();

        //2.构造分页构造器
        Page pageInfo = new Page(current, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<WmUser> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件

        //3.执行查询
        wmUserService.page(pageInfo, queryWrapper);
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
    public ResponseEntity<WmUser> selectOne(@PathVariable Serializable id) {
        return ResponseEntity.ok(wmUserService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param wmUser 实体对象
     * @return 新增结果
     */
    @ApiOperation("新增数据")
    @PostMapping
    public ResponseEntity<String> add(@RequestBody WmUser wmUser) {
        wmUserService.save(wmUser);
        return ResponseEntity.ok("新增成功！");
    }

    /**
     * 修改数据
     *
     * @param wmUser 实体对象
     * @return 修改结果
     */
    @ApiOperation("修改数据")
    @PutMapping
    public ResponseEntity<String> edit(@RequestBody WmUser wmUser) {
        wmUserService.updateById(wmUser);
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
        wmUserService.removeById(id);
        return ResponseEntity.ok("删除成功！");
    }
}

