package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 文章内容表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("article_content")
public class ArticleContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * Markdown内容
     */
    private String contentMd;

    /**
     * HTML内容
     */
    private String contentHtml;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    public String getContentMd() {
        return contentMd;
    }

    public void setContentMd(String contentMd) {
        this.contentMd = contentMd;
    }
    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    @Override
    public String toString() {
        return "ArticleContent{" +
            "articleId=" + articleId +
            ", contentMd=" + contentMd +
            ", contentHtml=" + contentHtml +
        "}";
    }
}
