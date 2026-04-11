package com.vagrant.nanoblog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

//主页显示文章卡片
@Data
public class ArticleHomeVO {
    /**文章ID*/
    private Long id;

    /*文章标识*/
    private String articleSlug;

    /*标题*/
    private String title;

    /*摘要*/
    private String summary;

    /*封面图地址*/
    private String coverUrl;

    /*作者ID*/
    private Long authorId;

    /*作者昵称*/
    private String authorName;

    /*作者头像*/
    private String authorAvatar;

    /*分类ID*/
    private Long categoryId;

    /*分类名称*/
    private String categoryName;

    /*标签名称列表*/
    private List<String> tags;

    /*是否置顶*/
    private Boolean isTop;

    /*是否推荐*/
    private Boolean isFeatured;

    /*浏览量*/
    private Long viewCount;

    /*点赞数*/
    private Long likeCount;

    /*评论数*/
    private Long commentCount;

    /*发布时间*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime publishTime;
}
