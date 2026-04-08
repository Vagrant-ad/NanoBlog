package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.vagrant.nanoblog.mapper.CommentMapper;
import com.vagrant.nanoblog.pojo.Comment;
import com.vagrant.nanoblog.pojo.CommentLike;
import com.vagrant.nanoblog.mapper.CommentLikeMapper;
import com.vagrant.nanoblog.service.ICommentLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评论点赞表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-04-09
 */
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements ICommentLikeService {

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public void likeComment(Long commentId, Long userId) {
        // 检查是否已点赞
        CommentLike existing = commentLikeMapper.selectOne(
                new QueryWrapper<CommentLike>()
                        .eq("comment_id", commentId)
                        .eq("user_id", userId)
        );
        if (existing != null) {
            throw new RuntimeException("已经点赞过了");
        }

        // 插入点赞记录
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        commentLikeMapper.insert(like);

        // like_count + 1
        commentMapper.update(null, new UpdateWrapper<Comment>()
                .eq("id", commentId)
                .setSql("like_count = like_count + 1"));
    }

    @Override
    public void unlikeComment(Long commentId, Long userId) {
        // 删除点赞记录
        int deleted = commentLikeMapper.delete(
                new QueryWrapper<CommentLike>()
                        .eq("comment_id", commentId)
                        .eq("user_id", userId)
        );

        if (deleted > 0) {
            // like_count - 1，注意不能小于 0
            commentMapper.update(null, new UpdateWrapper<Comment>()
                    .eq("id", commentId)
                    .gt("like_count", 0)
                    .setSql("like_count = like_count - 1"));
        }
    }

    @Override
    public Boolean isLiked(Long commentId, Long userId) {
        CommentLike existing = commentLikeMapper.selectOne(
                new QueryWrapper<CommentLike>()
                        .eq("comment_id", commentId)
                        .eq("user_id", userId)
        );
        return existing != null;
    }
}