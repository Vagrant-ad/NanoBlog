package com.vagrant.nanoblog.service;

import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.pojo.Article;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
