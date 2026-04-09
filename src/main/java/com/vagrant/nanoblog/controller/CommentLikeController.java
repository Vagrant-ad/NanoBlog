package com.vagrant.nanoblog.controller;

import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.ICommentLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 评论点赞表 前端控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-04-09
 */
@RestController
@RequestMapping("/like/comment")
public class CommentLikeController {

    @Autowired
    private ICommentLikeService commentLikeService;

    /**
     * 点赞评论
     */
    @PostMapping("/{commentId}")
    public ResponseResult likeComment(@PathVariable Long commentId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseResult.errorResult(401, "请先登录");
        }
        
        try {
            commentLikeService.likeComment(commentId, loginUser.getId());
            return ResponseResult.okResult();
        } catch (RuntimeException e) {
            return ResponseResult.errorResult(400, e.getMessage());
        }
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/{commentId}")
    public ResponseResult unlikeComment(@PathVariable Long commentId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseResult.errorResult(401, "请先登录");
        }
        
        commentLikeService.unlikeComment(commentId, loginUser.getId());
        return ResponseResult.okResult();
    }

    /**
     * 查询当前用户是否已点赞该评论
     */
    @GetMapping("/{commentId}")
    public ResponseResult isLiked(@PathVariable Long commentId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        Long userId = loginUser != null ? loginUser.getId() : null;
        
        boolean liked = commentLikeService.isLiked(commentId, userId);
        return ResponseResult.okResult(liked);
    }
}
