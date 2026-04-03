package com.vagrant.nanoblog.service;

import com.vagrant.nanoblog.pojo.ArticleContent;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 文章内容表 服务类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface IArticleContentService extends IService<ArticleContent> {
    /**
     * 根据文章 ID 获取文章内容
     * @param articleId 文章 ID
     * @return 文章内容
     */
    ArticleContent getByArticleId(Long articleId);

}
