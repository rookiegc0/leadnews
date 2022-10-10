package com.heima.article.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.heima.model.article.entity.ApArticleContent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.service.ApArticleContentService;
import java.io.Serializable;

/**
 * 文章内容(ApArticleContent)表控制层
 *
 * @author makejava
 * @since 2022-09-08 23:02:03
 */
@Api(tags = "文章内容接口")
@RestController
@RequestMapping("/api/v1/apArticleContent")
@Slf4j
public class ApArticleContentController {
    /**
     * Service层对象
     */
    @Autowired
    private ApArticleContentService apArticleContentService;

    /**
     * 分页查询所有数据
     *
     * @param pageRequest      分页对象
     * @param apArticleContent 查询实体
     * @return 所有数据
     */
    @ApiOperation("分页查询所有数据")
    @GetMapping
    public ResponseEntity<Page<ApArticleContent>> paginQuery(ApArticleContent apArticleContent, PageRequest pageRequest) {
        //1.分页参数
        long current = pageRequest.getPageNumber();
        long pageSize = pageRequest.getPageSize();

        //2.构造分页构造器
        Page pageInfo = new Page(current, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<ApArticleContent> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件

        //3.执行查询
        apArticleContentService.page(pageInfo, queryWrapper);
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
    public ResponseEntity<ApArticleContent> selectOne(@PathVariable Serializable id) {
        return ResponseEntity.ok(apArticleContentService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param apArticleContent 实体对象
     * @return 新增结果
     */
    @ApiOperation("新增数据")
    @PostMapping
    public ResponseEntity<String> add(@RequestBody ApArticleContent apArticleContent) {
        apArticleContentService.save(apArticleContent);
        return ResponseEntity.ok("新增成功！");
    }

    /**
     * 修改数据
     *
     * @param apArticleContent 实体对象
     * @return 修改结果
     */
    @ApiOperation("修改数据")
    @PutMapping
    public ResponseEntity<String> edit(@RequestBody ApArticleContent apArticleContent) {
        apArticleContentService.updateById(apArticleContent);
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
        apArticleContentService.removeById(id);
        return ResponseEntity.ok("删除成功！");
    }
}

