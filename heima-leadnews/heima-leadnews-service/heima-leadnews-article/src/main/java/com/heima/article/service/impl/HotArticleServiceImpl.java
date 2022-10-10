package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.HotArticleConstants;
import com.heima.common.constants.LoadArticleType;
import com.heima.common.redis.CacheService;
import com.heima.model.article.entity.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.entity.WmChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.data.Json;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 2022/9/23.
 *
 * @author Chen Gao
 */
@Service
@Slf4j
public class HotArticleServiceImpl implements HotArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private IWemediaClient wemediaClient;

    @Autowired
    CacheService cacheService;

    /**
     * 计算热点文章
     */
    @Override
    public void computeHotArticle() {
        //1.查询前5天的文章数据
        Date dateParam = DateTime.now().minusDays(5).toDate();
        List<ApArticle> apArticleList = apArticleMapper.findArticleListByLast5days(dateParam);
        //2.计算文章的分值
        List<HotArticleVo> hotArticleVoList = computeHotArticle(apArticleList);
        //3.为每个频道缓存30条分值较高的文章数据
        cacheTagToRedis(hotArticleVoList);
    }

    /**
     * 计算文章分值
     * @param apArticleList
     * @return
     */
    private List<HotArticleVo> computeHotArticle(List<ApArticle> apArticleList) {

        List<HotArticleVo> hotArticleVoList = new ArrayList<>();

        if (apArticleList != null && apArticleList.size() > 0){
            for (ApArticle apArticle : apArticleList) {
                HotArticleVo hot = new HotArticleVo();
                BeanUtils.copyProperties(apArticle, hot);
                Long score = computeScore(apArticle);
                hot.setScore(score);
                hotArticleVoList.add(hot);
            }
        }

        return null;
    }

    /**
     * 具体的计算每篇文章分值的具体方法
     * @param apArticle
     * @return
     */
    private Long computeScore(ApArticle apArticle) {
        Long score = Long.valueOf(0);
        if (apArticle.getLikes() != null){
            score += apArticle.getLikes()*HotArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getViews() != null){
            score += apArticle.getViews()*HotArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null){
            score += apArticle.getLikes()*HotArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;
    }

    /**
     * 为每个频道存30条分值较高的文章数据
     * @param hotArticleVoList
     */
    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        //每个频道缓存30条分值较高的文章数据
        ResponseResult responseResult = wemediaClient.getChannels();
        if (responseResult.getCode().equals(200)){
            String channelJson = JSON.toJSONString(responseResult.getData());
            List<WmChannel> wmChannels = JSON.parseArray(channelJson, WmChannel.class);
            //检索出每个频道的文章
            if (wmChannels != null && wmChannels.size() > 0){
                for (WmChannel wmChannel : wmChannels) {
                    List<HotArticleVo> hotArticleVos = hotArticleVoList.stream()
                            .filter(x -> x.getChannelId().equals(wmChannel.getId())).collect(Collectors.toList());
                    //给文章按照频道区分完了之后，现在开始各个频道内部要开始排序了,取30条分值较高的文章存入redis，key是频道id，value是30条文章
                    sortAndCache(hotArticleVos, HotArticleConstants.HOT_ARTICLE_FIRST_PAGE+wmChannel.getId());
                }

            }
        }
        //设置首页推荐数据，不关心频道的那种，全部统计
        //给文章进行排序，取30条分值较高的文章存入redis key是频道id value是30条不分频道的总体热点文章数据
        sortAndCache(hotArticleVoList, HotArticleConstants.HOT_ARTICLE_FIRST_PAGE+"__all__");
    }

    /**
     * 排序且缓存数据
     * @param hotArticleVos
     * @param key
     */
    private void sortAndCache(List<HotArticleVo> hotArticleVos, String key) {
        hotArticleVos = hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
        if (hotArticleVos.size() > 30){
            hotArticleVos = hotArticleVos.subList(0, 30);
        }
        cacheService.set(key, JSON.toJSONString(hotArticleVos));
    }

//
//    @Autowired
//    private ApArticleMapper apArticleMapper;
//
//    /**
//     * 计算热点文章
//     */
//    @Override
//    public void computeHotArticle() {
//        //1.查询前5天的文章数据
//        Date dateParam = DateTime.now().minusDays(5).toDate();
//        List<ApArticle> apArticleList = apArticleMapper.findArticleListByLast5days(dateParam);
//
//        //2.计算文章的分值
//        List<HotArticleVo> hotArticleVoList = computeHotArticle(apArticleList);
//
//        //3.为每个频道缓存30条分值较高的文章
//        cacheTagRedis(hotArticleVoList);
//    }
//
//    @Autowired
//    private IWemediaClient wemediaClient;
//
//    @Autowired
//    private CacheService cacheService;
//
//    /**
//     * 为每个频道缓存30条分值较高的文章
//     */
//    private void cacheTagRedis(List<HotArticleVo> hotArticleVoList){
//        //每个频道缓存30条分值较高的数据
//        ResponseResult channels = wemediaClient.getChannels();
//        if (channels.getCode().equals(200)){
//            String channelJSON = JSON.toJSONString(channels.getData());
//            List<WmChannel> wmChannels = JSON.parseArray(channelJSON, WmChannel.class);
//            //检索出每个频道的文章
//            if (wmChannels != null && wmChannels.size() > 0){
//                for (WmChannel wmChannel : wmChannels) {
//                    List<HotArticleVo> hotArticleVos = hotArticleVoList.stream().filter(x -> x.getChannelId().equals(wmChannel.getId()))
//                            .collect(Collectors.toList());
//                    sortAndCache(hotArticleVos, LoadArticleType.HOT_ARTICLE_FIRST_PAGE + wmChannel.getId());
//                }
//            }
//        }
//        //设置推荐数据
//        //给文章进行排序，取30条分值较高的文章存入redis key:频道id value：30条分值较高的文章
//        sortAndCache(hotArticleVoList, LoadArticleType.HOT_ARTICLE_FIRST_PAGE + LoadArticleType.DEFAULT_TAG);
//    }
//
//    private void sortAndCache(List<HotArticleVo> hotArticleVos, String key) {
//        //给文章进行排序，取30条分值较高的文章存入redis key:频道id value：30条分值较高的文章
//        hotArticleVos = hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
//                .collect(Collectors.toList());
//        if (hotArticleVos.size() > 30){
//            hotArticleVos = hotArticleVos.subList(0, 30);
//        }
//        cacheService.set(key, JSON.toJSONString(hotArticleVos));
//    }
//
//    /**
//     * 计算文章分值
//     * @param apArticleList
//     * @return
//     */
//    private List<HotArticleVo> computeHotArticle(List<ApArticle> apArticleList) {
//
//        List<HotArticleVo> hotArticleList = new ArrayList<>();
//
//        if (apArticleList != null && apArticleList.size() > 0){
//            for (ApArticle apArticle : apArticleList) {
//                HotArticleVo hot = new HotArticleVo();
//                BeanUtils.copyProperties(apArticle, hot);
//                Long score = computeScore(apArticle);
//                hot.setScore(score);
//                hotArticleList.add(hot);
//            }
//        }
//        return hotArticleList;
//    }
//
//    /**
//     * 计算文章的具体分数，是上一份方法的一个分支方法
//     * @param apArticle
//     * @return
//     */
//    private Long computeScore(ApArticle apArticle) {
//        Long score = Long.valueOf(0);
//        if (apArticle.getLikes() != null){
//            score += apArticle.getLikes() * LoadArticleType.HOT_ARTICLE_LIKE_WEIGHT;
//        }
//        if (apArticle.getViews() != null){
//            score += apArticle.getViews();
//        }
//        if (apArticle.getComment() != null){
//            score += apArticle.getComment() * LoadArticleType.HOT_ARTICLE_COMMENT_WEIGHT;
//        }
//        if (apArticle.getCollection() != null){
//            score += apArticle.getLikes() * LoadArticleType.HOT_ARTICLE_COLLECTION_WEIGHT;
//        }
//        return score;
//    }
}
