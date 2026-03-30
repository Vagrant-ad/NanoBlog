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
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * <p>
 * 文章表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
    private final ArticleContentMapper articleContentMapper;
    private final ArticleMapper articleMapper;
    //tag & 关联表
    @Getter
    private final TagMapper tagMapper;
    @Getter
    private final ArticleTagMapper articleTagMapper;

    private static final Parser MD_PARSER = Parser.builder()
            .extensions(Arrays.asList(
                    TablesExtension.create(),
                    TaskListItemsExtension.create(),
                    AutolinkExtension.create(),
                    StrikethroughExtension.create()
            ))
            .build();

    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder()
            .extensions(Arrays.asList(
                    TablesExtension.create(),
                    TaskListItemsExtension.create(),
                    AutolinkExtension.create(),
                    StrikethroughExtension.create()
            ))
            .build();

    @Override
    @Transactional
    public Long publishArticle(ArticlePublishDTO dto, Long userId) {

        // 1. Markdown → HTML
        String html = HTML_RENDERER.render(MD_PARSER.parse(dto.getContentMd()));

        // 2. 保存 article（用 MyBatis-Plus 内置方法）
        Article article = new Article();
        article.setAuthorId(userId);
        article.setCategoryId(dto.getCategoryId());
        article.setArticleTitle(dto.getArticleTitle());
        article.setArticleSummary(dto.getArticleSummary());

        // 封面图：前端上传后把URL放进coverUrl
        if (StringUtils.hasText(dto.getCoverUrl())) {
            article.setCoverImageUrl(dto.getCoverUrl());
        }

        // 状态：前端传0(草稿) 1(发布)
        // 兜底默认发布
        int status = (dto.getStatus() != null) ? dto.getStatus() : 1;
        article.setStatus(status);

        // 只有发布状态才设置发布时间
        if (status == 1) {
            article.setPublishTime(LocalDateTime.now());
        }

        this.save(article); // MP方法

        // 3. 保存 content
        ArticleContent content = new ArticleContent();
        content.setArticleId(article.getId());
        content.setContentMd(dto.getContentMd());
        content.setContentHtml(html);

        articleContentMapper.insert(content);
        // 处理标签
        if (!CollectionUtils.isEmpty(dto.getTags())) {
            saveArticleTags(article.getId(), dto.getTags());
        }
        return article.getId();
    }
    @Override
    public void saveArticleTags(Long articleId, List<String> tagNames) {
        for (String tagName : tagNames) {
            if (!StringUtils.hasText(tagName)) continue;

            // 查tag表，有则复用，无则新建
            Tag tag = tagMapper.selectOne(
                    new QueryWrapper<Tag>().eq("tag_name", tagName).eq("is_deleted", 0)
            );

            if (tag == null) {
                tag = new Tag();
                tag.setTagName(tagName);
                // slug简单处理为小写+去空格
                tag.setTagSlug(tagName.toLowerCase().replaceAll("\\s+", "-"));
                tag.setStatus(1);
                tagMapper.insert(tag);
            }

            // 写 article_tag 关联
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
        //查询状态为1的文章按发布时间降序
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .orderByDesc("publish_time");

        IPage<Article> articlePage = this.page(pageInfo, queryWrapper);

        /*// 日志：打印核心数据
        System.out.println("===== 调试日志 =====");
        System.out.println("1. 分页参数：page=" + page + ", size=" + size);
        System.out.println("2. 查询条件：status=1");
        System.out.println("3. 总记录数：" + articlePage.getTotal());
        System.out.println("4. 当前页记录数：" + articlePage.getRecords().size());
        // 打印第一条Article数据（看字段是否有值）
        if (!articlePage.getRecords().isEmpty()) {
            Article first = articlePage.getRecords().get(0);
            System.out.println("5. 第一条文章数据：id=" + first.getId() +
                    ", title=" + first.getArticleTitle() +
                    ", publishTime=" + first.getPublishTime());
        }*/

        // 转换成VO
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

/*        // 新增日志打印VO结果
        System.out.println("6. VO总记录数：" + result.getTotal());
        System.out.println("7. VO当前页记录数：" + result.getRecords().size());
        if (!result.getRecords().isEmpty()) {
            System.out.println("8. 第一条VO数据：" + result.getRecords().get(0));
        }
        System.out.println("======================================");*/


        return result;
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long id) {
        //先浏览量自增
        articleMapper.updateViewCount(id);
        Article article = this.getById(id);
        if (article == null || article.getIsDeleted() == 1) return null;

        ArticleContent content = articleContentMapper.selectById(id);

        // 批量查该文章的标签
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

    @Override
    public IPage<ArticleHomeVO> getHomeArticleList(Integer page, Integer size,String keyword,String sortBy) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 8;

        Page<ArticleHomeVO> pageInfo = new Page<>(page, size);

        // 1. 查首页文章基础数据
        List<ArticleHomeVO> records = articleMapper.getHomeArticlePage(pageInfo,keyword, sortBy);

        if (records == null || records.isEmpty()) {
            pageInfo.setRecords(records);
            return pageInfo;
        }

        // 2. 收集当前页文章ID
        List<Long> articleIds = records.stream()
                .map(ArticleHomeVO::getId)
                .collect(Collectors.toList());

        // 3. 批量查标签
        List<Map<String, Object>> tagRows = articleMapper.getTagsByArticleIds(articleIds);

        // 4. 组装 articleId -> tags
        Map<Long, List<String>> tagMap = tagRows.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row.get("articleId")).longValue(),
                        Collectors.mapping(row -> (String) row.get("tagName"), Collectors.toList())
                ));

        // 5. 填充 tags
        records.forEach(vo -> vo.setTags(tagMap.getOrDefault(vo.getId(), Collections.emptyList())));
        pageInfo.setRecords(records);
        return pageInfo;
    }
    //个人中心相关方法

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
        // 1. 校验文章归属
        Article article = this.getById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权编辑此文章");
        }

        // 2. 更新 article 主表
        article.setArticleTitle(dto.getArticleTitle());
        article.setArticleSummary(dto.getArticleSummary());
        article.setCategoryId(dto.getCategoryId());
        if (StringUtils.hasText(dto.getCoverUrl())) {
            article.setCoverImageUrl(dto.getCoverUrl());
        }

        // 状态变更：如果从草稿改为发布，补充发布时间
        int newStatus = (dto.getStatus() != null) ? dto.getStatus() : article.getStatus();
        if (newStatus == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        article.setStatus(newStatus);
        this.updateById(article);

        // 3. 更新正文内容
        if (StringUtils.hasText(dto.getContentMd())) {
            String html = HTML_RENDERER.render(MD_PARSER.parse(dto.getContentMd()));
            ArticleContent content = articleContentMapper.selectById(articleId);
            if (content != null) {
                content.setContentMd(dto.getContentMd());
                content.setContentHtml(html);
                articleContentMapper.updateById(content);
            } else {
                // 兜底：如果content记录不存在则新建
                content = new ArticleContent();
                content.setArticleId(articleId);
                content.setContentMd(dto.getContentMd());
                content.setContentHtml(html);
                articleContentMapper.insert(content);
            }
        }

        // 4. 更新标签：先删旧关联，再写新关联
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
        // 软删除
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

    /**
     * Article 分页结果 → ArticleManageVO 分页结果（公共转换，含标签批量查询）
     */
    private IPage<ArticleManageVO> convertToManageVO(IPage<Article> articlePage) {
        Page<ArticleManageVO> result = new Page<>();
        result.setTotal(articlePage.getTotal());
        result.setCurrent(articlePage.getCurrent());
        result.setSize(articlePage.getSize());

        if (CollectionUtils.isEmpty(articlePage.getRecords())) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 批量查标签
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
}
