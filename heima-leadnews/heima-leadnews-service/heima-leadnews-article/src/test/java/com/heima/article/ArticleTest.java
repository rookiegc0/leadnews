package com.heima.article;

import com.heima.article.mapper.ApArticleMapper;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.entity.ApArticle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

/**
 * Created on 2022/9/8.
 *测试类和被测试类最好是同一个包，所以这里的包名就这样设置了
 * @author Chen Gao
 */
@SpringBootTest
public class ArticleTest {

    @Autowired
    ApArticleMapper apArticleMapper;

    @Test
    void ApArticleMapper() {
        ArticleHomeDto articleHomeDto = new ArticleHomeDto();
        articleHomeDto.setTag("__all__");
        articleHomeDto.setSize(10);
        articleHomeDto.setMaxBehotTime(new Date(0L));
        articleHomeDto.setMinBehotTime(new Date(20000000000000L));
        Integer loadType = 1;
        List<ApArticle> articles = apArticleMapper.selectListWith(articleHomeDto, loadType);
        articles.forEach(a -> {
            System.out.println(a);
        });
    }
}
