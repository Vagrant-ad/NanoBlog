package com.vagrant.nanoblog.controller;


import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.service.IArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 文章表 前端控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
    private final IArticleService articleService;

    @PostMapping("/publish")
    public Long publish(@RequestBody ArticlePublishDTO dto) {

        Long userId = 2L; // 后面替换成登录用户

        return articleService.publishArticle(dto, userId);
    }
}
