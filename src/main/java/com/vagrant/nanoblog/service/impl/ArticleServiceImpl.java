package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.mapper.ArticleContentMapper;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.pojo.ArticleContent;
import com.vagrant.nanoblog.service.IArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import com.vagrant.nanoblog.vo.ArticleListVO;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
    private final ArticleContentMapper articleContentMapper;
    private final ArticleMapper articleMapper;
    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public ArticleServiceImpl(ArticleContentMapper articleContentMapper, ArticleMapper articleMapper) {
        this.articleContentMapper = articleContentMapper;
        this.articleMapper = articleMapper;
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

        // 1. 查 article
        Article article = this.getById(id);
        if (article == null) {
            return null;
        }

        // 2. 查 content
        ArticleContent content = articleContentMapper.selectById(id);


        // 3. 组装 VO
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getArticleTitle());
        vo.setCategoryId(article.getCategoryId());
        vo.setPublishTime(article.getPublishTime());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        if (content != null) {
            vo.setContent(content.getContentHtml());
        }


        return vo;
    }

    @Override
    public IPage<ArticleHomeVO> getHomeArticleList(Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 8;

        Page<ArticleHomeVO> pageInfo = new Page<>(page, size);

        // 1. 查首页文章基础数据
        List<ArticleHomeVO> records = articleMapper.getHomeArticlePage(pageInfo);

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
}
