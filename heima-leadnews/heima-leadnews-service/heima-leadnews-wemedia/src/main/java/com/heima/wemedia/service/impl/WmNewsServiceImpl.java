package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.article.ArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.constants.WmNewsCoverType;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.constants.WmNewsStatus;
import com.heima.common.exception.CustomException;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.entity.*;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.utils.threads.ThreadLocalUtils;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmNewsMaterialService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自媒体图文内容信息表(WmNews)表服务实现类
 *
 * @author makejava
 * @since 2022-09-09 11:45:52
 */
@Transactional
@Service("wmNewsService")
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    /**
     * 内容列表查询所有文章
     * @param pageReqDto
     * @return
     */
    @Override
    public ResponseResult listNews(WmNewsPageReqDto pageReqDto) {
        pageReqDto.checkParam();
        //1.调用page()方法完成分页查询
        Integer currentPage = pageReqDto.getPage();
        Integer pagesize = pageReqDto.getSize();
        Short status = pageReqDto.getStatus();
        Date beginPubdate = pageReqDto.getBeginPubdate();
        Date endPubdate = pageReqDto.getEndPubdate();
        Integer channelId = pageReqDto.getChannelId();
        String keyword = pageReqDto.getKeyword();

        //1.调用page()完成分页查询
        LambdaQueryWrapper<WmNews> lqw = new LambdaQueryWrapper<>();
        //2.关联用户查询
        lqw.eq(WmNews::getUserId, ThreadLocalUtils.getFromThreadLocal().getId());
        //关联自媒体文章状态查询
        lqw.eq(status != null,WmNews::getStatus, status);
        lqw.eq(channelId != null,WmNews::getChannelId, channelId);
        lqw.like(StringUtils.isEmpty(keyword),WmNews::getTitle, keyword);
        //关联自媒体文章发布时间范围
        Boolean condition = beginPubdate != null && endPubdate != null && (endPubdate.compareTo(beginPubdate)==1);
//        Boolean condition = true;
        lqw.between(condition,WmNews::getPublishTime,beginPubdate,endPubdate);

        //排序
        lqw.orderByDesc(WmNews::getPublishTime);

        Page<WmNews> page = page(new Page<WmNews>(currentPage, pagesize), lqw);
        //2.构建pageresponseresult
        ResponseResult pageResponseResult = new PageResponseResult(currentPage, pagesize, (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }


    @Autowired
    WmNewsMaterialService newsMaterialService;

    @Autowired
    WmMaterialService materialService;

    @Autowired
    WmNewsTaskService taskService;

    @Override
    public ResponseResult submit(WmNewsDto newsDto) {
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(newsDto, wmNews);
        Integer id = wmNews.getId();
        //关联用户
        wmNews.setUserId(ThreadLocalUtils.getFromThreadLocal().getId());
        //创建时间
        wmNews.setCreatedTime(new Date());
        //关联封面图片类型type
        Integer type = Integer.parseInt(newsDto.getType());
        //封面图片
        List<String> imagesFromCover = newsDto.getImages();
        //解析提交文章内容：拿到所有图片
        List<Map> content = JSONArray.parseArray(wmNews.getContent(), Map.class);
        List<String> imagesFromContent = content.stream().filter(map -> {
            if (map.get("type").equals("image")) {
                return true;
            }
            return false;
        }).map(m -> (String)m.get("value")).collect(Collectors.toList());
        //当用户选择自动：则需要根据自动业务规则计算type以及封面的图面
        if (type.equals(WmNewsCoverType.COVER_AUTO)){
            int size =imagesFromContent.size();
            if (size >= 0 && size < 1){
                type = WmNewsCoverType.NO_PICTURE;
            }
            if (size >= 1 && size < 3){
                type = WmNewsCoverType.ONE_PICTURE;
                imagesFromContent = imagesFromContent.stream().limit(1).collect(Collectors.toList());
            }
            if (size >= 3){
                type = WmNewsCoverType.MORE_PICTURE;
                imagesFromContent = imagesFromContent.stream().limit(3).collect(Collectors.toList());
            }
        }
        wmNews.setType(type);
        //关联封面图片
        wmNews.setImages(imagesFromContent.stream().collect(Collectors.joining(",")));
        //关联上架下架属性
        wmNews.setEnable(1);
        //1.判断id是否有值，如果有值则修改（还需要解除文章和素材之间的关系），如果没有值则新增
        if (id == null){
            this.save(wmNews);
        }else {
            newsMaterialService.remove(Wrappers.<WmNewsMaterial>lambdaQuery().eq(id != null, WmNewsMaterial::getNewsId,id));
            this.updateById(wmNews);
        }
        //2.判断status状态，看是否为草稿，不是草稿的话关联内容图片与素材关系和封面中的图片与素材关系
        if (!newsDto.getStatus().equals(WmNewsStatus.DRAFT)){
            //向wm_news_material插入正文中图片素材id和文章wmnews id
            saveBatchNewsMaterial(wmNews,imagesFromContent,0);
            //向wm_news_material插入封面中图片素材id和文章wmnews id
            saveBatchNewsMaterial(wmNews,imagesFromCover,1);
        }
        ResponseResult responseResult = ResponseResult.okResult("提交成功！");
        //host+image===>在文章内容列表中展示封面图片
        responseResult.setHost("");

        //对news文章进行审核
//        autoScan(wmNews);//这个方法是异步方法，合理！！
        taskService.addNewsToTask(wmNews.getId(), wmNews.getPublishTime());
        return responseResult;

    }

    @Autowired
    private KafkaTemplate<String ,String > kafkaTemplate;

    /**
     * 文章上下架
     * @param newsDto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto newsDto) {
        //1.检查参数
        if (newsDto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.查询文章
        WmNews wmNews = getById(newsDto.getId());
        if (wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //3.判断文章是否已经发布
        if (wmNews.getStatus().equals(WmNewsStatus.PUBLISHED)){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //4.修改文章enable
        if (newsDto.getEnable() != null && newsDto.getEnable() > -1 && newsDto.getEnable() < 2){
            update(Wrappers.<WmNews>lambdaUpdate().set(WmNews::getEnable, newsDto.getEnable())
                    .eq(WmNews::getId,wmNews.getId()));

            //做个判断，如果没有做改动则不需要发布消息
            if (newsDto.getEnable() != null && newsDto.getEnable() > -1 && newsDto.getEnable() < 2){
                //发送消息，通知article修改文章的配置
                Map<String ,Object> map = new HashMap<>();
                map.put("articleId", wmNews.getArticleId());
                map.put("enable", newsDto.getEnable());
                kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC, JSON.toJSONString(map));
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(), "修改成功");
    }

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ArticleClient articleClient;

    @Autowired
    private WmUserMapper userMapper;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmSensitiveMapper sensitiveMapper;

    @Autowired
    private Tess4jClient tess4jClient;

    /**
     * 自动审核函数
     * @param wmNews
     */
    @Async
    public void autoScan(WmNews wmNews) {
        if (wmNews == null){
            throw new CustomException(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //如果提交的是待审核的文章才会进行审核，新增提交待审核，修改提交待审核
        if (wmNews.getStatus() == WmNewsStatus.SUBMIT){
            //1.方法定义及提取文本和图片：提取文本和图片是供接口内容审核使用的
            StringBuilder text = new StringBuilder();
            List<String> images = new ArrayList<>();
            //提取来源：content,iamges,title
            List<Map> content = JSONArray.parseArray(wmNews.getContent(), Map.class);
            if (!StringUtils.isEmpty(wmNews.getImages())){
                images.addAll(Arrays.asList(wmNews.getImages().split(",")));
            }
            if (!StringUtils.isEmpty(wmNews.getTitle())){
                text.append(wmNews.getTitle());
            }
            //解析content：分别获取文本和图片存入不同对象中
            content.forEach(map -> {
                if (map.get("type").equals("image")){
                    images.add((String) map.get("value"));
                }
                if (map.get("type").equals("text")){
                    text.append(map.get("value"));
                }
            });
            List<String> imageScan = images.stream().distinct().collect(Collectors.toList());
            //自定义敏感词审核,非第三方服务，基于DFA算法
            List<WmSensitive> sensitives = sensitiveMapper.selectList(null);
            List<String> sensitivewords = sensitives.stream().map(m -> m.getSensitives()).collect(Collectors.toList());
            SensitiveWordUtil.initMap(sensitivewords);

            Map<String, Integer> sensitivewordsMap = SensitiveWordUtil.matchWords(text.toString());
            //如果文本中存在敏感词，则集合大小一定不为0
            if (sensitivewordsMap.size() > 0){
                wmNews.setStatus(WmNewsStatus.AUDIT_FAILURE);
                wmNews.setReason("审核内容存在以下敏感词：" + sensitivewordsMap);
                this.updateById(wmNews);
                return;//审核失败，程序结束
            }

            //阿里审核文本：text
            try {
                Map map = greenTextScan.greeTextScan(text.toString());
                //获取map的建议：{suggestion=block,label=contraband},根据建议来修改news表中status和reason
                if (updateNewsByScanResult(wmNews, map)) {
                    return;//审核失败就不要再往下走了，直接结束了，所以return结束
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            //审核图片：images
            try {
                List<byte[]> list = new ArrayList<>();
                imageScan.forEach(imageUrl -> {
                    byte[] bytes = fileStorageService.downLoadFile(imageUrl);
                    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                    BufferedImage bufferedImage = null;
                    String result = null;
                    try {
                        bufferedImage = ImageIO.read(in);
                        result = tess4jClient.doOCR(bufferedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (TesseractException e) {
                        e.printStackTrace();
                    }
                    //过滤文字
                    Map<String, Integer> sensitiveresultsMap = SensitiveWordUtil.matchWords(result);
                    //如果文本中存在敏感词，则集合大小一定不为0
                    if (sensitiveresultsMap.size() > 0){
                        wmNews.setStatus(WmNewsStatus.AUDIT_FAILURE);
                        wmNews.setReason("审核内容存在以下敏感词：" + sensitiveresultsMap);
                        this.updateById(wmNews);
                        return;//审核失败，程序结束
                    }
                    list.add(bytes);
                });
                Map map = greenImageScan.imageScan(list);
                //获取map的建议：{suggestion=block,label=contraband},根据建议来修改news表中status和reason
                if (updateNewsByScanResult(wmNews, map)) {
                    return;//审核失败就不要再往下走了，直接结束了，所以return结束
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //审核完成文本和图片之后，使用feign客户端来远程调用文章微服务保存app文章
            ArticleDto articleDto = new ArticleDto();
            BeanUtils.copyProperties(wmNews, articleDto);
            //关联作者
            articleDto.setAuthorId(wmNews.getUserId());
            //最后是保存到article表中，这里需要对比一下哪些属性需要自己设置一下
            WmUser wmUser = userMapper.selectById(wmNews.getUserId());
            if (wmUser != null){
                articleDto.setAuthorName(wmUser.getName());
            }
            WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
            if (wmChannel != null){
                articleDto.setChannelName(wmChannel.getName());
            }
            articleDto.setLayout(wmNews.getType());

            //如果news中的articleId不为空，塞进去
            if (wmNews.getArticleId() != null){
                articleDto.setId(wmNews.getArticleId());
            }
            //调用feign客户端远程调用文章服务接口：返回文章articleID,修改自媒体端文章状态
            ResponseResult responseResult = articleClient.saveArticle(articleDto);
            wmNews.setStatus(WmNewsStatus.PUBLISHED);
            wmNews.setArticleId((Long) responseResult.getData());
            this.updateById(wmNews);
        }

    }

    /**
     * 对审核结果进行处理以及回应函数
     * @param wmNews
     * @param map
     * @return
     */
    private boolean updateNewsByScanResult(WmNews wmNews, Map map) {
        if (map.get("suggestion").equals("block")){
            wmNews.setStatus(WmNewsStatus.AUDIT_FAILURE);
            wmNews.setReason("审核的内容存在违规，审核失败！");
            this.updateById(wmNews);
            return true;
        }
        if (map.get("suggestion").equals("review")){
            wmNews.setStatus(WmNewsStatus.MANUAL_REVIEW);
            wmNews.setReason("审核的内容存在不确定性，需要人工继续审核!");
            this.updateById(wmNews);
            return true;
        }
        return false;
    }

    /**
     * 通过给定的图片集合获取素材wmmaterial id
     */
    public void saveBatchNewsMaterial(WmNews wmNews,List<String> images,Integer type){
        List<WmMaterial> wmMaterials = materialService.list(Wrappers.<WmMaterial>lambdaQuery().
                in(images.size() > 0, WmMaterial::getUrl, images));

        List<WmNewsMaterial> wmNewsMaterials = wmMaterials.stream().map(m -> {
            WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
            wmNewsMaterial.setMaterialId(m.getId());
            wmNewsMaterial.setNewsId(wmNews.getId());
            wmNewsMaterial.setType(type);
            wmNewsMaterial.setOrd(wmMaterials.indexOf(m));
            return wmNewsMaterial;
        }).collect(Collectors.toList());
        newsMaterialService.saveBatch(wmNewsMaterials);
    }
}

