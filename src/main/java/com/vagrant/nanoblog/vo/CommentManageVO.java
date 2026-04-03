package com.vagrant.nanoblog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论管理 VO
 */
@Data
public class CommentManageVO {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private Long userId;
    private String authorNickname;
    private String commentContent;
    private Integer status;          // 0 正常 1 已删除
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

}
