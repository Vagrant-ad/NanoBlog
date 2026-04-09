package com.vagrant.nanoblog.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vagrant.nanoblog.pojo.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文章表 Mapper 接口
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface ArticleMapper extends BaseMapper<Article> {

    ArticleDetailVO getArticleDetailById(Long id);

    void updateViewCount(Long id);

    // 首页文章分页
    List<ArticleHomeVO> getHomeArticlePage(Page<ArticleHomeVO> page,
                                           @Param("keyword") String keyword,
                                           @Param("sortBy") String sortBy,
                                           @Param("categoryIds") List<Long> categoryIds,
                                           @Param("tagId") Long tagId);

    // 批量查文章标签，返回 articleId+tagName
    List<Map<String, Object>> getTagsByArticleIds(@Param("articleIds") List<Long> articleIds);

    // ===================== 【新增：按标签查询文章】 =====================
    IPage<ArticleHomeVO> getArticlePageByTagId(IPage<ArticleHomeVO> page, @Param("tagId") Long tagId);

    /**
     * 统计指定作者所有文章的点赞数总和
     */
    Long sumLikeCountByAuthor(@Param("authorId") Long authorId);
}