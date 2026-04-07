package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 评论点赞表
 * </p>
 *
 * @author vagrant
 * @since 2026-04-07
 */
@TableName("comment_like")
public class CommentLike implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long commentId;

    private LocalDateTime createTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CommentLike{" +
            "userId=" + userId +
            ", commentId=" + commentId +
            ", createTime=" + createTime +
        "}";
    }
}
