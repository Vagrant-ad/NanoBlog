package com.vagrant.nanoblog.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class CommentVO {
    private Long id;
    private Long  userId;
    private String nickname;
    private String avatarUrl;
    private String commentContent;
    private LocalDateTime createTime;
    private Long likeCount;
    private List<CommentVO> replies;
}

