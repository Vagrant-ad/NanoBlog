package com.vagrant.nanoblog.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vagrant.nanoblog.pojo.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ArticleMapper extends BaseMapper<Article> {

    // 原有方法
    ArticleDetailVO getArticleDetailById(Long id);
    void updateViewCount(Long id);

    List<ArticleHomeVO> getHomeArticlePage(
            Page<ArticleHomeVO> page,
            @Param("keyword") String keyword,
            @Param("sortBy") String sortBy,
            @Param("categoryId") Long categoryId
    );

    List<Map<String, Object>> getTagsByArticleIds(@Param("articleIds") List<Long> articleIds);

    // 新增：按标签ID查询文章
    List<ArticleHomeVO> getArticlePageByTagId(
            Page<ArticleHomeVO> page,
            @Param("tagId") Long tagId
    );
}