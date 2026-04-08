package com.vagrant.nanoblog.service;

import com.vagrant.nanoblog.pojo.ArticleLike;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 文章点赞表 服务类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface IArticleLikeService extends IService<ArticleLike> {

    /**
     * 点赞文章
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void likeArticle(Long articleId, Long userId);

    /**
     * 取消点赞
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void unlikeArticle(Long articleId, Long userId);

    /**
     * 查询当前用户是否已点赞
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    Boolean isLiked(Long articleId, Long userId);

}