package com.vagrant.nanoblog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailVO {

    private Long id;
    private String title;
    private String content;       //HTML，供详情页渲染
    private String contentMd;     //Markdown，供编辑器回填

    private Long categoryId;
    private String categoryName;

    private String coverImageUrl; //封面图，供编辑器回填

    private List<String> tags;    //标签列表，供编辑器回填

    private Integer status;       //状态，供编辑器判断是草稿还是已发布
    //作者信息
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime publishTime;

    private Long viewCount;
    private Long likeCount;
}