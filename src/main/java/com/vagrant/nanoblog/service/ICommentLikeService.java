package com.vagrant.nanoblog.service;

import com.vagrant.nanoblog.pojo.CommentLike;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 评论点赞表 服务类
 * </p>
 *
 * @author vagrant
 * @since 2026-04-07
 */
public interface ICommentLikeService extends IService<CommentLike> {

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void likeComment(Long commentId, Long userId);

    /**
     * 取消点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void unlikeComment(Long commentId, Long userId);

    /**
     * 查询当前用户是否已点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    Boolean isLiked(Long commentId, Long userId);

}
