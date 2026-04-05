package com.vagrant.nanoblog.controller;

import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.Tag;
import com.vagrant.nanoblog.service.ITagService;
import com.vagrant.nanoblog.vo.TagVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签表 前端控制器
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    private final ITagService tagService;

    // 改用构造器注入，解决 "Field injection is not recommended" 警告
    public TagController(ITagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 获取所有标签（给标签云/标签页面使用）
     */
    @GetMapping("/list")
    public ResponseResult<List<TagVO>> getTagList() {
        return ResponseResult.okResult(tagService.getTagsWithCount());
    }
}