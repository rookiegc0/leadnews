package com.heima.article.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;

import java.util.List;

import com.heima.common.constants.LoadArticleType;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.entity.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.service.ApArticleService;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 文章表(ApArticle)表控制层
 *
 * @author makejava
 * @since 2022-09-07 23:48:37
 */
@Api(tags = "文章表接口")
@RestController
@RequestMapping("/api/v1/article")
@Slf4j
public class ApArticleController {
    /**
     * Service层对象
     */
    @Autowired
    private ApArticleService apArticleService;

    /**
     * 加载所有频道的文章
     * 1.请求：POST
     * 2.参数
     * 2.响应结果
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto articleHomeDto){
        return ResponseResult.okResult(apArticleService.loadArticle2(articleHomeDto,LoadArticleType.LOAD_NEW,true));
    }

    /**
     * 下拉加载最新默认文章
     * 1.请求： POST
     * 2.参数
     * 3.响应结果
     */
    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto articleHomeDto){
        return ResponseResult.okResult(apArticleService.loadArticle(articleHomeDto,LoadArticleType.LOAD_NEW));
    }


    /**
     * 上拉加载更多文章
     * 1.POST
     * 2.参数
     * 3.响应结果
     */
    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto articleHomeDto){
        return ResponseResult.okResult(apArticleService.loadArticle(articleHomeDto,LoadArticleType.LOAD_MORE));
    }

    /**
     *在自媒体端文章审核的时候，需要保存文章
     */
    @PostMapping("/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto articleDto){
        return apArticleService.saveArticle(articleDto);
    }

    /**
     * 分页查询所有数据
     *
     * @param pageRequest 分页对象
     * @param apArticle   查询实体
     * @return 所有数据
     */
    @ApiOperation("分页查询所有数据")
    @GetMapping
    public ResponseEntity<Page<ApArticle>> paginQuery(ApArticle apArticle, PageRequest pageRequest) {
        //1.分页参数
        long current = pageRequest.getPageNumber();
        long pageSize = pageRequest.getPageSize();

        //2.构造分页构造器
        Page pageInfo = new Page(current, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<ApArticle> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件

        //3.执行查询
        apArticleService.page(pageInfo, queryWrapper);
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
    public ResponseEntity<ApArticle> selectOne(@PathVariable Serializable id) {
        return ResponseEntity.ok(apArticleService.getById(id));
    }


    /**
     * 修改数据
     *
     * @param apArticle 实体对象
     * @return 修改结果
     */
    @ApiOperation("修改数据")
    @PutMapping
    public ResponseEntity<String> edit(@RequestBody ApArticle apArticle) {
        apArticleService.updateById(apArticle);
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
        apArticleService.removeById(id);
        return ResponseEntity.ok("删除成功！");
    }
}

