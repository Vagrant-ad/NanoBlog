package com.vagrant.nanoblog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 *用于个人主页中"我的文章"和"草稿箱"列表展示
 */
@Data
public class ArticleManageVO {
    private Long id;
    private String articleTitle;
    private String articleSummary;
    private String coverImageUrl;
    private Integer status;//0草稿 1发布 2归档
    private List<String> tags;
    private Long categoryId;
    private String categoryName;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime publishTime;
}