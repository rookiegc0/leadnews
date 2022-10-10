package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.article.entity.ApArticleContent;
import com.heima.article.mapper.ApArticleContentMapper;
import org.springframework.stereotype.Service;
import com.heima.article.service.ApArticleContentService;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * 文章内容(ApArticleContent)表服务实现类
 *
 * @author makejava
 * @since 2022-09-08 23:02:04
 */
@Service("apArticleContentService")
public class ApArticleContentServiceImpl extends ServiceImpl<ApArticleContentMapper, ApArticleContent> implements ApArticleContentService {

    @Override
    public boolean saveBatch(Collection<ApArticleContent> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<ApArticleContent> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<ApArticleContent> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(ApArticleContent entity) {
        return false;
    }

    @Override
    public ApArticleContent getOne(Wrapper<ApArticleContent> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Map<String, Object> getMap(Wrapper<ApArticleContent> queryWrapper) {
        return null;
    }

    @Override
    public <V> V getObj(Wrapper<ApArticleContent> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }
}

