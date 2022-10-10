package com.heima.wemedia.controller;




import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.entity.WmNews;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.heima.wemedia.service.WmNewsService;
import java.io.Serializable;
/**
 * 自媒体图文内容信息表(WmNews)表控制层
 *
 * @author makejava
 * @since 2022-09-09 11:45:51
 */
@Api(tags = "自媒体图文内容信息表接口")
@RestController
@RequestMapping("/api/v1/news")
@Slf4j
public class WmNewsController {
    /**
     * Service层对象
     */
    @Lazy
    @Autowired
    private WmNewsService wmNewsService;

    /**
     * 分页查询自媒体端文章数据
     */
    @PostMapping("/list")
    public ResponseResult listNews(@RequestBody WmNewsPageReqDto pageReqDto){
        return wmNewsService.listNews(pageReqDto);
    }

    /**
     * 发布文章
     */
    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto newsDto){
        return wmNewsService.submit(newsDto);
    }

    /**
     *上下架功能
     */
    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDto newsDto){
        return wmNewsService.downOrUp(newsDto);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation("通过主键查询单条数据")
    @GetMapping("{id}")
    public ResponseEntity<WmNews> selectOne(@PathVariable Serializable id) {
        return ResponseEntity.ok(wmNewsService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param wmNews 实体对象
     * @return 新增结果
     */
    @ApiOperation("新增数据")
    @PostMapping
    public ResponseEntity<String> add(@RequestBody WmNews wmNews) {
        wmNewsService.save(wmNews);
        return ResponseEntity.ok("新增成功！");
    }

    /**
     * 修改数据
     *
     * @param wmNews 实体对象
     * @return 修改结果
     */
    @ApiOperation("修改数据")
    @PutMapping
    public ResponseEntity<String> edit(@RequestBody WmNews wmNews) {
        wmNewsService.updateById(wmNews);
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
        wmNewsService.removeById(id);
        return ResponseEntity.ok("删除成功！");
    }
}

