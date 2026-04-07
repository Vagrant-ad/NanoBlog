package com.vagrant.nanoblog.vo;

import lombok.Data;
import java.util.List;

@Data
public class CategoryTreeVO {
    private Long id;
    private String categoryName;
    private String categorySlug;
    private Integer sortOrder;
    private List<CategoryTreeVO> children;
}