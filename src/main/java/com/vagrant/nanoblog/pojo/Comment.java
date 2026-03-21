package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 评论表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long userId;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 回复用户ID
     */
    private Long replyToUserId;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 状态
     */
    private Integer status;

    private Long likeCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    public Long getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(Long replyToUserId) {
        this.replyToUserId = replyToUserId;
    }
    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "Comment{" +
            "id=" + id +
            ", articleId=" + articleId +
            ", userId=" + userId +
            ", parentId=" + parentId +
            ", replyToUserId=" + replyToUserId +
            ", commentContent=" + commentContent +
            ", status=" + status +
            ", likeCount=" + likeCount +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            ", isDeleted=" + isDeleted +
        "}";
    }
}
