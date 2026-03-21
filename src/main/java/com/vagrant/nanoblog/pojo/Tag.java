package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 标签表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("tag")
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 标签标识
     */
    private String tagSlug;

    /**
     * 标签颜色
     */
    private String tagColor;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    public String getTagSlug() {
        return tagSlug;
    }

    public void setTagSlug(String tagSlug) {
        this.tagSlug = tagSlug;
    }
    public String getTagColor() {
        return tagColor;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
        return "Tag{" +
            "id=" + id +
            ", tagName=" + tagName +
            ", tagSlug=" + tagSlug +
            ", tagColor=" + tagColor +
            ", status=" + status +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            ", isDeleted=" + isDeleted +
        "}";
    }
}
