package com.vagrant.nanoblog.controller;


import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IUserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 用户关注表 前端控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Controller
@RequestMapping("/follow")
public class UserFollowController {

    @Autowired
    private IUserFollowService userFollowService;

    /**
     * 关注用户
     */
    @PostMapping("/{userId}")
    @ResponseBody
    public ResponseResult follow(@PathVariable Long userId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseResult.errorResult(401, "请先登录");
        }

        try {
            userFollowService.follow(userId, loginUser.getId());
            return ResponseResult.okResult();
        } catch (RuntimeException e) {
            return ResponseResult.errorResult(400, e.getMessage());
        }
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{userId}")
    @ResponseBody
    public ResponseResult unfollow(@PathVariable Long userId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseResult.errorResult(401, "请先登录");
        }

        try {
            userFollowService.unfollow(userId, loginUser.getId());
            return ResponseResult.okResult();
        } catch (RuntimeException e) {
            return ResponseResult.errorResult(400, e.getMessage());
        }
    }

    /**
     * 查询是否已关注
     */
    @GetMapping("/check/{userId}")
    @ResponseBody
    public ResponseResult isFollowing(@PathVariable Long userId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");

        //未登录返回false，不弹登录提示
        if (loginUser == null) {
            return ResponseResult.okResult(false);
        }

        Boolean following = userFollowService.isFollowing(userId, loginUser.getId());
        return ResponseResult.okResult(following);
    }

    /**
     * 粉丝列表（关注我的人）
     */
    @GetMapping("/fans/{userId}")
    @ResponseBody
    public ResponseResult getFansList(@PathVariable Long userId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        Long currentUserId = loginUser != null ? loginUser.getId() : null;

        try {
            return ResponseResult.okResult(userFollowService.getFansList(userId, currentUserId));
        } catch (RuntimeException e) {
            return ResponseResult.errorResult(400, e.getMessage());
        }
    }

    /**
     * 我关注的人列表
     */
    @GetMapping("/following/{userId}")
    @ResponseBody
    public ResponseResult getFollowingList(@PathVariable Long userId, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        Long currentUserId = loginUser != null ? loginUser.getId() : null;

        try {
            return ResponseResult.okResult(userFollowService.getFollowingList(userId, currentUserId));
        } catch (RuntimeException e) {
            return ResponseResult.errorResult(400, e.getMessage());
        }
    }
}
