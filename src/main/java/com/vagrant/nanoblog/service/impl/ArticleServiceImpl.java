package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.mapper.ArticleContentMapper;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.pojo.ArticleContent;
import com.vagrant.nanoblog.service.IArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 文章表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
    private final ArticleContentMapper articleContentMapper;

    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public ArticleServiceImpl(ArticleContentMapper articleContentMapper) {
        this.articleContentMapper = articleContentMapper;
    }

    @Override
    @Transactional
    public Long publishArticle(ArticlePublishDTO dto, Long userId) {

        // 1. Markdown → HTML
        String html = renderer.render(parser.parse(dto.getContentMd()));

        // 2. 保存 article（用 MyBatis-Plus 内置方法）
        Article article = new Article();
        article.setAuthorId(userId);
        article.setCategoryId(dto.getCategoryId());
        article.setArticleTitle(dto.getArticleTitle());
        article.setArticleSummary(dto.getArticleSummary());
        article.setStatus(1);
        article.setPublishTime(LocalDateTime.now());

        this.save(article); // MP方法

        // 3. 保存 content
        ArticleContent content = new ArticleContent();
        content.setArticleId(article.getId());
        content.setContentMd(dto.getContentMd());
        content.setContentHtml(html);

        articleContentMapper.insert(content);

        return article.getId();
    }
}
