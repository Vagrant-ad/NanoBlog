package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 文章收藏表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("article_favorite")
public class ArticleFavorite implements Serializable {

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
        return "ArticleFavorite{" +
            "userId=" + userId +
            ", articleId=" + articleId +
            ", createTime=" + createTime +
        "}";
    }
}
