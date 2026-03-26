package com.vagrant.nanoblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.pojo.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import com.vagrant.nanoblog.vo.ArticleListVO;

/**
 * <p>
 * 文章表 服务类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface IArticleService extends IService<Article> {
    Long publishArticle(ArticlePublishDTO dto, Long userId);

    IPage<ArticleListVO> getArticleList(Integer page, Integer size);
    ArticleDetailVO getArticleDetail(Long id);
    IPage<ArticleHomeVO> getHomeArticleList(Integer page, Integer size);
}
