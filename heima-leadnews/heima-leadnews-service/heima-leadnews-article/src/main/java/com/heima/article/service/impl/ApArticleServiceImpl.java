package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.HotArticleConstants;
import com.heima.common.constants.LoadArticleType;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.entity.ApArticle;
import com.heima.model.article.entity.ApArticleConfig;
import com.heima.model.article.entity.ApArticleContent;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.mess.ArticleVisitStreamMess;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章表(ApArticle)表服务实现类
 *
 * @author makejava
 * @since 2022-09-07 23:48:37
 */
@Service("apArticleService")
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    ApArticleMapper apArticleMapper;

    public static final Integer MAX_SIZE = 50;

    @Override
    public ArrayList<HotArticleVo> loadArticle(ArticleHomeDto articleHomeDto, Integer loadType) {
        //校验频道：tag 是否为null,是空的话，给出一个默认值，也就是推荐频道的tag
        String tag = articleHomeDto.getTag();
        if (StringUtils.isEmpty(tag)) {
            articleHomeDto.setTag("__all__");
        }
        //校验显示文章条数：size是否为0，也不能超过最大值
        Integer size = articleHomeDto.getSize();
        if (size == 0 || size == null){
            articleHomeDto.setSize(10);
        }
        size = size > MAX_SIZE ? MAX_SIZE : size;
        articleHomeDto.setSize(size);
        //校验时间：minBehotTime,maxBehotTime为空的话，需要给系统时间
        if (articleHomeDto.getMinBehotTime() == null || articleHomeDto.getMaxBehotTime() == null){
            articleHomeDto.setMaxBehotTime(new Date());
            articleHomeDto.setMinBehotTime(new Date());
        }
        //校验加载类型，loadNew没有给值的话，给出默认值
        if (loadType != LoadArticleType.LOAD_MORE && loadType != LoadArticleType.LOAD_NEW){
            loadType = LoadArticleType.LOAD_MORE;
        }
        List<ApArticle> articles = apArticleMapper.selectListWith(articleHomeDto,loadType);
        ArrayList<HotArticleVo> hotArticles = new ArrayList<>();
        BeanUtils.copyProperties(articles, hotArticles);
        return hotArticles;
    }

    @Autowired
    private CacheService cacheService;

    /**
     * 加载文章列表，并且关注是否首页加载
     * @param articleHomeDto
     * @param loadType
     * @param firstPage
     * @return
     */
    @Override
    public List<HotArticleVo> loadArticle2(ArticleHomeDto articleHomeDto, Integer loadType, boolean firstPage) {
        if (firstPage){
            String jsonStr = cacheService.get(LoadArticleType.HOT_ARTICLE_FIRST_PAGE + articleHomeDto.getTag());
            if (!StringUtils.isEmpty(jsonStr)){
                List<HotArticleVo> hotArticleVos = JSON.parseArray(jsonStr, HotArticleVo.class);
                return hotArticleVos;
            }
        }
        return loadArticle(articleHomeDto, loadType);
    }

    @Autowired
    ApArticleConfigService articleConfigService;

    @Autowired
    ApArticleContentServiceImpl articleContentService;

    @Autowired
    private ArticleFreemarkerServiceImpl articleFreemarkerService;


    @Override
    public ResponseResult saveArticle(ArticleDto articleDto) {
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(articleDto, apArticle);
        Long id = articleDto.getId();
        //如果id为空，需要插入表，三个跟文章相关的表都要插入
        if (id == null){
            //保存ap_article
            this.save(apArticle);
            //保存ap_article_config
            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setArticleId(apArticle.getId());
            articleConfigService.save(apArticleConfig);
            //保存ap_article_content
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(articleDto.getContent());
            articleContentService.save(apArticleContent);
        }else {
            this.updateById(apArticle);
            articleContentService.update(Wrappers.<ApArticleContent>lambdaUpdate()
                    .eq(id != null, ApArticleContent::getArticleId, id)
                    .set(ApArticleContent::getContent, articleDto.getContent()));
        }

        //异步调用 生成静态上传到minio中
        articleFreemarkerService.buildArticleToMinIO(apArticle,articleDto.getContent());
        //结果返回 文章的id
        return ResponseResult.okResult((apArticle.getId()==null)? "" : apArticle.getId());
    }

    /**
     * 更新文章的分值 同时更新缓存中热点文章的数据
     * @param mess
     */
    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        //1.更新文章的阅读喜欢等等相关的数量
        ApArticle apArticle = updateArticle(mess);
        //2.计算文章的分值
        Long score = computeScore(apArticle);
        score = score * 3;
        //3.替换当前文章对应频道的热点数据
        replaceDataToRedis(HotArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId(), apArticle, score);
        //4.替换推荐对应的热点数据,不关心频道的那部分热点数据
        replaceDataToRedis(HotArticleConstants.HOT_ARTICLE_FIRST_PAGE + "__all__", apArticle, score);
    }

    /**
     * 替换数据且存入到redis中
     * @param HOT_ARTICLE_FIRST_PAGE
     * @param apArticle
     * @param score
     */
    private void replaceDataToRedis(String HOT_ARTICLE_FIRST_PAGE, ApArticle apArticle, Long score) {
        String articleListAllStr = cacheService.get(HOT_ARTICLE_FIRST_PAGE);
        if (StringUtils.isNotBlank(articleListAllStr)){
            List<HotArticleVo> hotArticleVoList = JSON.parseArray(articleListAllStr, HotArticleVo.class);

            boolean flag = true;

            //如果缓存中存在该文章，只更新分值
            for (HotArticleVo hotArticleVo : hotArticleVoList) {
                if (hotArticleVo.getId().equals(apArticle.getId())){
                    hotArticleVo.setScore(score);
                    flag = false;
                    break;
                }
            }
            //如果缓存中不存在，查询缓存中分值最小的一条数据，进行分值的比较，如果当前文章分值大于缓存中的数据，则替换
            if (flag && hotArticleVoList.size() >= 30){
                hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore)
                        .reversed()).collect(Collectors.toList());
                HotArticleVo lasthot = hotArticleVoList.get(hotArticleVoList.size() - 1);
                if (lasthot.getScore() < score){
                    hotArticleVoList.remove(lasthot);
                    HotArticleVo hot = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle, hot);
                    hot.setScore(score);
                    hotArticleVoList.add(hot);
                }
            }else {
                //新增数据
                HotArticleVo hot = new HotArticleVo();
                BeanUtils.copyProperties(apArticle, hot);
                hot.setScore(score);
                hotArticleVoList.add(hot);
            }
            //缓存到redis中
            hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore)
                    .reversed()).collect(Collectors.toList());
            cacheService.set(HOT_ARTICLE_FIRST_PAGE
                    ,JSON.toJSONString(hotArticleVoList));
        }
    }

    /**
     * 更新文章行为数据
     * @param mess
     */
    private ApArticle updateArticle(ArticleVisitStreamMess mess) {
        ApArticle apArticle = getById(mess.getArticleId());
        apArticle.setCollection(apArticle.getCollection()==null?0:apArticle.getCollection()+mess.getCollect());
        apArticle.setComment(apArticle.getCollection()==null?0:apArticle.getCollection()+mess.getComment());
        apArticle.setLikes(apArticle.getCollection()==null?0:apArticle.getCollection()+mess.getLike());
        apArticle.setViews(apArticle.getCollection()==null?0:apArticle.getCollection()+mess.getView());
        updateById(apArticle);
        return apArticle;
    }

    /**
     * 计算文章具体分值
     * @param apArticle
     * @return
     */
    private Long computeScore(ApArticle apArticle) {
        Long score = Long.valueOf(0);
        if (apArticle.getLikes() != null){
            score += apArticle.getLikes()* HotArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getViews() != null){
            score += apArticle.getViews()*HotArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null){
            score += apArticle.getLikes()*HotArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;
    }
}

