package com.vagrant.nanoblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 后台用户管理控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private IUserService userService;

    /**
     * 分页查询用户列表
     * GET /admin/user/list
     */
    @GetMapping("/list")
    public ResponseResult getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {

        Page<User> userPage = new Page<>(page, size);
        IPage<User> result = userService.getUserList(userPage, username);

        // 封装返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        data.put("pages", result.getPages());

        return ResponseResult.okResult(data);
    }

    /**
     * 修改用户状态
     * PUT /admin/user/{userId}/status
     */
    @PutMapping("/{userId}/status")
    public ResponseResult updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status,
            HttpSession session) {

        // 校验状态值
        if (status == null || (status != 0 && status != 1)) {
            return ResponseResult.errorResult(400, "无效的状态值");
        }

        // 获取当前登录用户，防止禁用自己
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser != null && loginUser.getId().equals(userId)) {
            return ResponseResult.errorResult(400, "不能修改自己的状态");
        }

        return userService.updateUserStatus(userId, status);
    }

    /**
     * 删除用户（软删除）
     * DELETE /admin/user/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseResult deleteUser(
            @PathVariable Long userId,
            HttpSession session) {

        // 获取当前登录用户，防止删除自己
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser != null && loginUser.getId().equals(userId)) {
            return ResponseResult.errorResult(400, "不能删除自己");
        }

        return userService.deleteUser(userId);
    }

}
