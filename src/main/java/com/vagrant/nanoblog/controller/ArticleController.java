package com.vagrant.nanoblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import com.vagrant.nanoblog.vo.ArticleListVO;
import com.vagrant.nanoblog.vo.ArticleManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final IArticleService articleService;

    // 获取登录用户id
    private Long getCurrentUserId(HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            throw new RuntimeException("请先登录");
        }
        return loginUser.getId();
    }

    @PostMapping("/publish")
    public ResponseResult<Long> publish(@RequestBody ArticlePublishDTO dto, HttpSession session) {
        Long userId = getCurrentUserId(session);
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
            @RequestParam(defaultValue = "time") String sortBy,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseResult.okResult(articleService.getHomeArticleList(page, size, keyword, sortBy, categoryId));
    }

    // 按分类查询文章
    @GetMapping("/list/category/{categoryId}")
    public ResponseResult<IPage<ArticleListVO>> listByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseResult.okResult(articleService.listByCategory(categoryId, pageNum, pageSize));
    }

    // 按标签查询文章
    @GetMapping("/list/tag/{tagId}")
    public ResponseResult<IPage<ArticleHomeVO>> listByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseResult.okResult(articleService.listByTag(tagId, pageNum, pageSize));
    }

    // ===================== 个人中心相关接口 =====================
    @GetMapping("/my/published")
    public ResponseResult<IPage<ArticleManageVO>> myPublished(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size, HttpSession session) {
        Long userId = getCurrentUserId(session);
        return ResponseResult.okResult(articleService.getMyPublished(userId, page, size));
    }

    @GetMapping("/my/drafts")
    public ResponseResult<IPage<ArticleManageVO>> myDrafts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size, HttpSession session) {
        Long userId = getCurrentUserId(session);
        return ResponseResult.okResult(articleService.getMyDrafts(userId, page, size));
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(
            @PathVariable Long id,
            @RequestBody ArticlePublishDTO dto, HttpSession session) {
        Long userId = getCurrentUserId(session);
        articleService.updateArticle(id, dto, userId);
        return ResponseResult.okResult();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id, HttpSession session) {
        Long userId = getCurrentUserId(session);
        articleService.deleteArticle(id, userId);
        return ResponseResult.okResult();
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<Void> publishDraft(@PathVariable Long id, HttpSession session) {
        Long userId = getCurrentUserId(session);
        articleService.publishDraft(id, userId);
        return ResponseResult.okResult();
    }
}