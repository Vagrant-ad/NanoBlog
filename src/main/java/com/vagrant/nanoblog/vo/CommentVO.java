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
    
    private Long parentId;
    //回复用户字段
    private Long replyToUserId;
    private String replyToNickname;
    private List<CommentVO> replies;
}
