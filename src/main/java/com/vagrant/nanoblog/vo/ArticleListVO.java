package com.vagrant.nanoblog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class ArticleListVO {
    //文章列表返回对象
    private Long id;
    private String articleTitle;
    private String articleSummary;
    private Long categoryId;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime publishTime;
}