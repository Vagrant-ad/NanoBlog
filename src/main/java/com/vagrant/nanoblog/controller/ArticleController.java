package com.vagrant.nanoblog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.service.impl.ArticleServiceImpl;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import com.vagrant.nanoblog.vo.ArticleListVO;
import com.vagrant.nanoblog.vo.ArticleManageVO;
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
    public ResponseResult<Long> publish(@RequestBody ArticlePublishDTO dto) {

        Long userId = 2L; // TODO 后面替换成登录用户
        Long articleId = articleService.publishArticle(dto, userId);
        return ResponseResult.okResult(articleId);
    }
    @GetMapping("/list")
    public ResponseResult<IPage<ArticleListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseResult.okResult(articleService.getArticleList(page, size));
    }
    @GetMapping("/{id}")
    public ResponseResult<ArticleDetailVO> getArticleDetail(@PathVariable Long id) {
        ArticleDetailVO article = articleService.getArticleDetail(id);
        System.out.println(article);
        return ResponseResult.okResult(article);
    }

    @GetMapping("/home")
    public ResponseResult<IPage<ArticleHomeVO>> homePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "time") String sortBy
    ) {
        return ResponseResult.okResult(articleService.getHomeArticleList(page, size,keyword,sortBy));
    }

    // ===================== 个人中心相关接口 =====================

    /**
     * 我的已发布文章列表
     * GET /article/my/published
     */
    @GetMapping("/my/published")
    public ResponseResult<IPage<ArticleManageVO>> myPublished(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = 2L; // TODO: 替换为登录用户
        return ResponseResult.okResult(articleService.getMyPublished(userId, page, size));
    }

    /**
     * 我的草稿列表
     * GET /article/my/drafts
     */
    @GetMapping("/my/drafts")
    public ResponseResult<IPage<ArticleManageVO>> myDrafts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = 2L; // TODO: 替换为登录用户
        return ResponseResult.okResult(articleService.getMyDrafts(userId, page, size));
    }

    /**
     * 更新文章（编辑已发布或草稿）
     * PUT /article/{id}
     */
    @PutMapping("/{id}")
    public ResponseResult<Void> update(
            @PathVariable Long id,
            @RequestBody ArticlePublishDTO dto) {
        Long userId = 2L; // TODO: 替换为登录用户
        articleService.updateArticle(id, dto, userId);
        return ResponseResult.okResult();
    }

    /**
     * 软删除文章
     * DELETE /article/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        Long userId = 2L; // TODO: 替换为登录用户
        articleService.deleteArticle(id, userId);
        return ResponseResult.okResult();
    }

    /**
     * 草稿发布
     * POST /article/{id}/publish
     */
    @PostMapping("/{id}/publish")
    public ResponseResult<Void> publishDraft(@PathVariable Long id) {
        Long userId = 2L; // TODO: 替换为登录用户
        articleService.publishDraft(id, userId);
        return ResponseResult.okResult();
    }

}
