package com.vagrant.nanoblog.controller;


import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IArticleLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 文章点赞表 前端控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Controller
@RequestMapping("/like")
public class ArticleLikeController {

    @Autowired
    private IArticleLikeService articleLikeService;

    /**
     * 点赞文章
     */
    @PostMapping("/article/{articleId}")
    @ResponseBody
    public ResponseResult likeArticle(@PathVariable Long articleId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseResult.errorResult(401, "请先登录");
        }

        try {
            articleLikeService.likeArticle(articleId, loginUser.getId());
            return ResponseResult.okResult();
        } catch (RuntimeException e) {
            return ResponseResult.errorResult(400, e.getMessage());
        }
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/article/{articleId}")
    @ResponseBody
    public ResponseResult unlikeArticle(@PathVariable Long articleId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseResult.errorResult(401, "请先登录");
        }

        try {
            articleLikeService.unlikeArticle(articleId, loginUser.getId());
            return ResponseResult.okResult();
        } catch (RuntimeException e) {
            return ResponseResult.errorResult(400, e.getMessage());
        }
    }

    /**
     * 查询当前用户是否已点赞
     */
    @GetMapping("/article/{articleId}")
    @ResponseBody
    public ResponseResult isLiked(@PathVariable Long articleId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");

        // 未登录返回 false，不弹登录提示
        if (loginUser == null) {
            return ResponseResult.okResult(false);
        }

        Boolean liked = articleLikeService.isLiked(articleId, loginUser.getId());
        return ResponseResult.okResult(liked);
    }
}
