package com.vagrant.nanoblog.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticlePublishDTO {

    private String articleTitle;
    private String articleSummary;
    private Long categoryId;

    private String contentMd; // Markdown

    private String coverUrl;
    private List<String> tags;
    private Integer status;
}