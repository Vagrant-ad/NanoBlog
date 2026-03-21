package com.vagrant.nanoblog.dto;

import lombok.Data;

@Data
public class ArticlePublishDTO {

    private String articleTitle;
    private String articleSummary;
    private Long categoryId;

    private String contentMd; // Markdown
}