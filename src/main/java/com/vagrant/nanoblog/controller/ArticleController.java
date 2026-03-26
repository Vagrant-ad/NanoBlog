package com.vagrant.nanoblog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.service.impl.ArticleServiceImpl;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

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
    @GetMapping("/list")
    public IPage<ArticleListVO> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return articleService.getArticleList(page, size);
    }
    @GetMapping("/{id}")
    public ResponseResult<ArticleDetailVO> getArticleDetail(@PathVariable Long id) {
        ArticleDetailVO article = articleService.getArticleDetail(id);
        System.out.println(article);
        return ResponseResult.okResult(article);
    }
}
