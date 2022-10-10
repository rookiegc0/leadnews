package com.heima.article;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.service.ApArticleService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.entity.ApArticle;
import com.heima.model.article.entity.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created on 2022/9/8.
 *
 * @author Chen Gao
 */
@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {

    @Autowired
    private ApArticleContentMapper articleContentMapper;

    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleService apArticleService;

    @Test
    public void createStaticUrlTest() throws IOException, TemplateException {
        //1.获取文章内容
        ApArticleContent apArticleContent = articleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery()
                .eq(ApArticleContent::getArticleId, "1383828014629179393L"));
        if (apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())){
            //2.文章内容通过freemarker生成html文件
            Template template = configuration.getTemplate("article.ftl");
            //数据模型
            Map<String,Object> content = new HashedMap();
            content.put("content", JSONArray.parseArray(apArticleContent.getContent()));
            StringWriter out = new StringWriter();
            //合成
            template.process(content,out);
            //3.把html文件上传到minio中
            InputStream in = new ByteArrayInputStream(out.toString().getBytes());
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", in);
            //4.修改ap_article表，保存static_url字段
            apArticleService.update(Wrappers.<ApArticle>lambdaUpdate().eq(ApArticle::getId,apArticleContent.getArticleId())
                    .set(ApArticle::getStaticUrl,path));
        }
    }

//    @Test
//    public static void main(String[] args) throws IOException, ServerException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//
//        FileInputStream fileInputStream = new FileInputStream("d:\\tmp\\js\\axios.min.js");
//
//        //1.获取minio的链接信息，创建一个minio客户端
//        MinioClient minioClient = MinioClient.builder()
//                .credentials("minio", "minio123")
//                .endpoint("http://192.168.200.130:9000")
//                .build();
//        //2.上传
//        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
//                .object("plugins/js/axios.min.js")//文件名词
//                .contentType("text/js")//文件类型
//                .bucket("leadnews")//桶名称 与minio管理界面的桶的名称要一致
//                .stream(fileInputStream, fileInputStream.available(), -1)
//                .build();
//        minioClient.putObject(putObjectArgs);
//        //访问路径
////        System.out.println("http://192.168.200.130:9000/leadnews/list.html");
//    }

}
