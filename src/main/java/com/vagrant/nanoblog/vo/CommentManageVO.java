package com.vagrant.nanoblog.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论管理VO（后台管理用）
 */
@Data
public class CommentManageVO {
    private Long id;
    private String commentContent;
    private String authorNickname;   // 评论者昵称
    private String articleTitle;     // 所属文章标题
    private Long articleId;
    private LocalDateTime createTime;
    private Integer status;
}
