package com.vagrant.nanoblog.vo;

import lombok.Data;

/**
 *标签展示VO(含关联文章数)
 */
@Data
public class TagVO {
    private Long id;
    private String tagName;
    private String tagColor;
    private Integer articleCount;//关联的文章数量
}