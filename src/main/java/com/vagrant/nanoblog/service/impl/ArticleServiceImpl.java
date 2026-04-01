package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.mapper.ArticleContentMapper;
import com.vagrant.nanoblog.mapper.ArticleTagMapper;
import com.vagrant.nanoblog.mapper.TagMapper;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.pojo.ArticleContent;
import com.vagrant.nanoblog.pojo.ArticleTag;
import com.vagrant.nanoblog.pojo.Tag;
import com.vagrant.nanoblog.service.IArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import com.vagrant.nanoblog.vo.ArticleListVO;
import com.vagrant.nanoblog.vo.ArticleManageVO;
import lombok.Getter;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
    private final ArticleContentMapper articleContentMapper;
    private final ArticleMapper articleMapper;
    @Getter
    private final TagMapper tagMapper;
    @Getter
    private final ArticleTagMapper articleTagMapper;

    private static final Parser MD_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    @Override
    @Transactional
    public Long publishArticle(ArticlePublishDTO dto, Long userId) {
        String html = HTML_RENDERER.render(MD_PARSER.parse(dto.getContentMd()));

        Article article = new Article();
        article.setAuthorId(userId);
        article.setCategoryId(dto.getCategoryId());
        article.setArticleTitle(dto.getArticleTitle());
        article.setArticleSummary(dto.getArticleSummary());

        if (StringUtils.hasText(dto.getCoverUrl())) {
            article.setCoverImageUrl(dto.getCoverUrl());
        }

        int status = (dto.getStatus() != null) ? dto.getStatus() : 1;
        article.setStatus(status);

        if (status == 1) {
            article.setPublishTime(LocalDateTime.now());
        }

        this.save(article);

        ArticleContent content = new ArticleContent();
        content.setArticleId(article.getId());
        content.setContentMd(dto.getContentMd());
        content.setContentHtml(html);

        articleContentMapper.insert(content);
        if (!CollectionUtils.isEmpty(dto.getTags())) {
            saveArticleTags(article.getId(), dto.getTags());
        }
        return article.getId();
    }

    @Override
    public void saveArticleTags(Long articleId, List<String> tagNames) {
        for (String tagName : tagNames) {
            if (!StringUtils.hasText(tagName)) continue;

            Tag tag = tagMapper.selectOne(
                    new QueryWrapper<Tag>().eq("tag_name", tagName).eq("is_deleted", 0)
            );

            if (tag == null) {
                tag = new Tag();
                tag.setTagName(tagName);
                tag.setTagSlug(tagName.toLowerCase().replaceAll("\\s+", "-"));
                tag.setStatus(1);
                tagMapper.insert(tag);
            }

            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tag.getId());
            articleTagMapper.insert(articleTag);
        }
    }

    @Override
    public IPage<ArticleListVO> getArticleList(Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 10;
        Page<Article> pageInfo = new Page<>(page, size);
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .orderByDesc("publish_time");

        IPage<Article> articlePage = this.page(pageInfo, queryWrapper);

        Page<ArticleListVO> result = new Page<>();
        result.setTotal(articlePage.getTotal());
        result.setCurrent(articlePage.getCurrent());
        result.setSize(articlePage.getSize());

        List<ArticleListVO> voList = articlePage.getRecords().stream().map(article -> {
            ArticleListVO vo = new ArticleListVO();
            vo.setId(article.getId());
            vo.setArticleTitle(article.getArticleTitle());
            vo.setArticleSummary(article.getArticleSummary());
            vo.setCategoryId(article.getCategoryId());
            vo.setViewCount(article.getViewCount());
            vo.setLikeCount(article.getLikeCount());
            vo.setCommentCount(article.getCommentCount());
            vo.setPublishTime(article.getPublishTime());
            return vo;
        }).collect(Collectors.toList());

        result.setRecords(voList);
        return result;
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long id) {
        articleMapper.updateViewCount(id);
        Article article = this.getById(id);
        if (article == null || article.getIsDeleted() == 1) return null;

        ArticleContent content = articleContentMapper.selectById(id);

        List<Map<String, Object>> tagRows = articleMapper.getTagsByArticleIds(
                Collections.singletonList(id));
        List<String> tags = tagRows.stream()
                .map(row -> (String) row.get("tagName"))
                .collect(Collectors.toList());

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getArticleTitle());
        vo.setCategoryId(article.getCategoryId());
        vo.setPublishTime(article.getPublishTime());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setStatus(article.getStatus());
        vo.setCoverImageUrl(article.getCoverImageUrl());
        vo.setTags(tags);

        if (content != null) {
            vo.setContent(content.getContentHtml());
            vo.setContentMd(content.getContentMd());
        }
        return vo;
    }

    // ===================== 【这里已经彻底改好】 =====================
    @Override
    public IPage<ArticleHomeVO> getHomeArticleList(Integer page, Integer size, String keyword, String sortBy, Long categoryId) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 8;

        Page<ArticleHomeVO> pageInfo = new Page<>(page, size);

        // 稳定传参，重启永不失效
        List<ArticleHomeVO> records = articleMapper.getHomeArticlePage(pageInfo, keyword, sortBy, categoryId);

        if (records == null || records.isEmpty()) {
            pageInfo.setRecords(records);
            return pageInfo;
        }

        List<Long> articleIds = records.stream()
                .map(ArticleHomeVO::getId)
                .collect(Collectors.toList());

        List<Map<String, Object>> tagRows = articleMapper.getTagsByArticleIds(articleIds);

        Map<Long, List<String>> tagMap = tagRows.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row.get("articleId")).longValue(),
                        Collectors.mapping(row -> (String) row.get("tagName"), Collectors.toList())
                ));

        records.forEach(vo -> vo.setTags(tagMap.getOrDefault(vo.getId(), Collections.emptyList())));
        pageInfo.setRecords(records);
        return pageInfo;
    }

    @Override
    public IPage<ArticleManageVO> getMyPublished(Long userId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 10;

        Page<Article> pageInfo = new Page<>(page, size);
        QueryWrapper<Article> qw = new QueryWrapper<Article>()
                .eq("author_id", userId)
                .eq("status", 1)
                .eq("is_deleted", 0)
                .orderByDesc("publish_time");

        IPage<Article> articlePage = this.page(pageInfo, qw);
        return convertToManageVO(articlePage);
    }

    @Override
    public IPage<ArticleManageVO> getMyDrafts(Long userId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 10;

        Page<Article> pageInfo = new Page<>(page, size);
        QueryWrapper<Article> qw = new QueryWrapper<Article>()
                .eq("author_id", userId)
                .eq("status", 0)
                .eq("is_deleted", 0)
                .orderByDesc("create_time");

        IPage<Article> articlePage = this.page(pageInfo, qw);
        return convertToManageVO(articlePage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long articleId, ArticlePublishDTO dto, Long userId) {
        Article article = this.getById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权编辑此文章");
        }

        article.setArticleTitle(dto.getArticleTitle());
        article.setArticleSummary(dto.getArticleSummary());
        article.setCategoryId(dto.getCategoryId());
        if (StringUtils.hasText(dto.getCoverUrl())) {
            article.setCoverImageUrl(dto.getCoverUrl());
        }

        int newStatus = (dto.getStatus() != null) ? dto.getStatus() : article.getStatus();
        if (newStatus == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        article.setStatus(newStatus);
        this.updateById(article);

        if (StringUtils.hasText(dto.getContentMd())) {
            String html = HTML_RENDERER.render(MD_PARSER.parse(dto.getContentMd()));
            ArticleContent content = articleContentMapper.selectById(articleId);
            if (content != null) {
                content.setContentMd(dto.getContentMd());
                content.setContentHtml(html);
                articleContentMapper.updateById(content);
            } else {
                content = new ArticleContent();
                content.setArticleId(articleId);
                content.setContentMd(dto.getContentMd());
                content.setContentHtml(html);
                articleContentMapper.insert(content);
            }
        }

        articleTagMapper.delete(
                new QueryWrapper<ArticleTag>().eq("article_id", articleId)
        );
        if (!CollectionUtils.isEmpty(dto.getTags())) {
            saveArticleTags(articleId, dto.getTags());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId, Long userId) {
        Article article = this.getById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权删除此文章");
        }
        article.setIsDeleted(1);
        this.updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishDraft(Long articleId, Long userId) {
        Article article = this.getById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权操作此文章");
        }
        if (article.getStatus() != 0) {
            throw new RuntimeException("只有草稿才能发布");
        }
        article.setStatus(1);
        article.setPublishTime(LocalDateTime.now());
        this.updateById(article);
    }

    private IPage<ArticleManageVO> convertToManageVO(IPage<Article> articlePage) {
        Page<ArticleManageVO> result = new Page<>();
        result.setTotal(articlePage.getTotal());
        result.setCurrent(articlePage.getCurrent());
        result.setSize(articlePage.getSize());

        if (CollectionUtils.isEmpty(articlePage.getRecords())) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        List<Long> ids = articlePage.getRecords().stream()
                .map(Article::getId).collect(Collectors.toList());
        List<Map<String, Object>> tagRows = articleMapper.getTagsByArticleIds(ids);
        Map<Long, List<String>> tagMap = tagRows.stream().collect(Collectors.groupingBy(
                row -> ((Number) row.get("articleId")).longValue(),
                Collectors.mapping(row -> (String) row.get("tagName"), Collectors.toList())
        ));

        List<ArticleManageVO> voList = articlePage.getRecords().stream().map(a -> {
            ArticleManageVO vo = new ArticleManageVO();
            vo.setId(a.getId());
            vo.setArticleTitle(a.getArticleTitle());
            vo.setArticleSummary(a.getArticleSummary());
            vo.setCoverImageUrl(a.getCoverImageUrl());
            vo.setStatus(a.getStatus());
            vo.setCategoryId(a.getCategoryId());
            vo.setViewCount(a.getViewCount());
            vo.setLikeCount(a.getLikeCount());
            vo.setCommentCount(a.getCommentCount());
            vo.setCreateTime(a.getCreateTime());
            vo.setPublishTime(a.getPublishTime());
            vo.setTags(tagMap.getOrDefault(a.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());

        result.setRecords(voList);
        return result;
    }

    @Override
    public IPage<ArticleListVO> listByCategory(Long categoryId, Integer pageNum, Integer pageSize) {
        Page<Article> page = new Page<>(pageNum, pageSize);

        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
                .eq("is_deleted", 0)
                .eq("category_id", categoryId)
                .orderByDesc("publish_time");

        IPage<Article> articlePage = this.page(page, wrapper);

        List<ArticleListVO> voList = articlePage.getRecords().stream().map(article -> {
            ArticleListVO vo = new ArticleListVO();
            vo.setId(article.getId());
            vo.setArticleTitle(article.getArticleTitle());
            vo.setArticleSummary(article.getArticleSummary());
            vo.setCategoryId(article.getCategoryId());
            vo.setViewCount(article.getViewCount());
            vo.setLikeCount(article.getLikeCount());
            vo.setCommentCount(article.getCommentCount());
            vo.setPublishTime(article.getPublishTime());
            return vo;
        }).collect(Collectors.toList());

        Page<ArticleListVO> resultPage = new Page<>();
        resultPage.setRecords(voList);
        resultPage.setTotal(articlePage.getTotal());
        resultPage.setSize(articlePage.getSize());
        resultPage.setCurrent(articlePage.getCurrent());

        return resultPage;
    }

    // ===================== 【新增：按标签查询文章】 =====================
    @Override
    public IPage<ArticleHomeVO> listByTag(Long tagId, Integer pageNum, Integer pageSize) {
        Page<ArticleHomeVO> page = new Page<>(pageNum, pageSize);
        // 调用你已写好的 Mapper 方法
        articleMapper.getArticlePageByTagId(page, tagId);
        return page;
    }
}