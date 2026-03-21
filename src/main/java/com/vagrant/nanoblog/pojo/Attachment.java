package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 附件表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("attachment")
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long uploaderId;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private String fileMd5;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
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
        return "Attachment{" +
            "id=" + id +
            ", uploaderId=" + uploaderId +
            ", fileName=" + fileName +
            ", fileUrl=" + fileUrl +
            ", fileType=" + fileType +
            ", fileSize=" + fileSize +
            ", fileMd5=" + fileMd5 +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            ", isDeleted=" + isDeleted +
        "}";
    }
}
