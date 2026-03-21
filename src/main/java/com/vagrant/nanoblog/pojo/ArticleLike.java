package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 文章点赞表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("article_like")
public class ArticleLike implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long articleId;

    private LocalDateTime createTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ArticleLike{" +
            "userId=" + userId +
            ", articleId=" + articleId +
            ", createTime=" + createTime +
        "}";
    }
}
