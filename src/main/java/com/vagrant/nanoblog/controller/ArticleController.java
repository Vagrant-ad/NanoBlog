package com.vagrant.nanoblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import com.vagrant.nanoblog.vo.ArticleListVO;
import com.vagrant.nanoblog.vo.ArticleManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        return ResponseResult.okResult(article);
    }

    @GetMapping("/home")
    public ResponseResult<IPage<ArticleHomeVO>> homePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size,
            @RequestParam(value = "key", required = false) String keyword,
            @RequestParam(defaultValue = "time") String sortBy,
            @RequestParam(value = "categoryId", required = false) Long categoryId
    ) {
        return ResponseResult.okResult(articleService.getHomeArticleList(page, size, keyword, sortBy, categoryId));
    }

    // ===================== 【分类接口：按分类查文章】 =====================
    @GetMapping("/list/category/{categoryId}")
    public ResponseResult<IPage<ArticleListVO>> listByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseResult.okResult(articleService.listByCategory(categoryId, pageNum, pageSize));
    }

    // ===================== 【新增：按标签查文章】 =====================
    @GetMapping("/list/tag/{tagId}")
    public ResponseResult<IPage<ArticleHomeVO>> listByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 这里直接调用 Mapper 新增的方法 (注意：你需要在 IArticleService 中定义 listByTag 方法)
        return ResponseResult.okResult(articleService.listByTag(tagId, pageNum, pageSize));
    }

    // ===================== 个人中心相关接口 =====================
    @GetMapping("/my/published")
    public ResponseResult<IPage<ArticleManageVO>> myPublished(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = 2L; // TODO: 替换为登录用户
        return ResponseResult.okResult(articleService.getMyPublished(userId, page, size));
    }

    @GetMapping("/my/drafts")
    public ResponseResult<IPage<ArticleManageVO>> myDrafts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = 2L; // TODO: 替换为登录用户
        return ResponseResult.okResult(articleService.getMyDrafts(userId, page, size));
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(
            @PathVariable Long id,
            @RequestBody ArticlePublishDTO dto) {
        Long userId = 2L; // TODO: 替换为登录用户
        articleService.updateArticle(id, dto, userId);
        return ResponseResult.okResult();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        Long userId = 2L; // TODO: 替换为登录用户
        articleService.deleteArticle(id, userId);
        return ResponseResult.okResult();
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<Void> publishDraft(@PathVariable Long id) {
        Long userId = 2L; // TODO: 替换为登录用户
        articleService.publishDraft(id, userId);
        return ResponseResult.okResult();
    }
}