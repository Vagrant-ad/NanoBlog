package com.vagrant.nanoblog.vo;

import java.time.LocalDateTime;

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