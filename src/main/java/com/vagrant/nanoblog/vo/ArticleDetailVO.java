package com.vagrant.nanoblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDetailVO {

    private Long id;
    private String title;
    private String content;

    private Long categoryId;
    private String categoryName;

    private LocalDateTime publishTime;

    private Long viewCount;
    private Long likeCount;


}