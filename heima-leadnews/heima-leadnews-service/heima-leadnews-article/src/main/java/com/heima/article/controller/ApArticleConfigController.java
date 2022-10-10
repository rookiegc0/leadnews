package com.heima.article.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;

import java.util.List;

import com.heima.model.article.entity.ApArticleConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.service.ApArticleConfigService;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 文章配置表(ApArticleConfig)表控制层
 *
 * @author makejava
 * @since 2022-09-07 23:48:37
 */
@Api(tags = "文章配置表接口")
@RestController
@RequestMapping("/api/v1/apArticleConfig")
@Slf4j
public class ApArticleConfigController {
    /**
     * Service层对象
     */
    @Autowired
    private ApArticleConfigService apArticleConfigService;

    /**
     * 分页查询所有数据
     *
     * @param pageRequest     分页对象
     * @param apArticleConfig 查询实体
     * @return 所有数据
     */
    @ApiOperation("分页查询所有数据")
    @GetMapping
    public ResponseEntity<Page<ApArticleConfig>> paginQuery(ApArticleConfig apArticleConfig, PageRequest pageRequest) {
        //1.分页参数
        long current = pageRequest.getPageNumber();
        long pageSize = pageRequest.getPageSize();

        //2.构造分页构造器
        Page pageInfo = new Page(current, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<ApArticleConfig> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件

        //3.执行查询
        apArticleConfigService.page(pageInfo, queryWrapper);
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
    public ResponseEntity<ApArticleConfig> selectOne(@PathVariable Serializable id) {
        return ResponseEntity.ok(apArticleConfigService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param apArticleConfig 实体对象
     * @return 新增结果
     */
    @ApiOperation("新增数据")
    @PostMapping
    public ResponseEntity<String> add(@RequestBody ApArticleConfig apArticleConfig) {
        apArticleConfigService.save(apArticleConfig);
        return ResponseEntity.ok("新增成功！");
    }

    /**
     * 修改数据
     *
     * @param apArticleConfig 实体对象
     * @return 修改结果
     */
    @ApiOperation("修改数据")
    @PutMapping
    public ResponseEntity<String> edit(@RequestBody ApArticleConfig apArticleConfig) {
        apArticleConfigService.updateById(apArticleConfig);
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
        apArticleConfigService.removeById(id);
        return ResponseEntity.ok("删除成功！");
    }
}

