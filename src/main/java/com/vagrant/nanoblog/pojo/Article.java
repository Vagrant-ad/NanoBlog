package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 文章表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("article")
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标题
     */
    private String articleTitle;

    /**
     * 文章标识
     */
    private String articleSlug;

    /**
     * 摘要
     */
    private String articleSummary;

    /**
     * 封面图
     */
    private String coverImageUrl;

    /**
     * 状态(0草稿 1发布 2归档)
     */
    private Integer status;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 是否推荐
     */
    private Integer isFeatured;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 收藏数
     */
    private Long favoriteCount;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }
    public String getArticleSlug() {
        return articleSlug;
    }

    public void setArticleSlug(String articleSlug) {
        this.articleSlug = articleSlug;
    }
    public String getArticleSummary() {
        return articleSummary;
    }

    public void setArticleSummary(String articleSummary) {
        this.articleSummary = articleSummary;
    }
    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }
    public Integer getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Integer isFeatured) {
        this.isFeatured = isFeatured;
    }
    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
    public Long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
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
        return "Article{" +
            "id=" + id +
            ", authorId=" + authorId +
            ", categoryId=" + categoryId +
            ", articleTitle=" + articleTitle +
            ", articleSlug=" + articleSlug +
            ", articleSummary=" + articleSummary +
            ", coverImageUrl=" + coverImageUrl +
            ", status=" + status +
            ", isTop=" + isTop +
            ", isFeatured=" + isFeatured +
            ", viewCount=" + viewCount +
            ", likeCount=" + likeCount +
            ", commentCount=" + commentCount +
            ", favoriteCount=" + favoriteCount +
            ", publishTime=" + publishTime +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            ", isDeleted=" + isDeleted +
        "}";
    }
}
