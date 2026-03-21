package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 文章标签关联表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("article_tag")
public class ArticleTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long articleId;

    private Long tagId;

    private LocalDateTime createTime;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ArticleTag{" +
            "articleId=" + articleId +
            ", tagId=" + tagId +
            ", createTime=" + createTime +
        "}";
    }
}
