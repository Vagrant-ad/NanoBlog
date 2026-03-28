package com.vagrant.nanoblog.controller;


import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;



/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    // 跳转到注册页面
    @GetMapping("/register")
    public String toRegister() {
        return "register"; // 对应register.html
    }

    // 处理注册请求
    @PostMapping("/doRegister")
    @ResponseBody
    public String doRegister(User user) {
        return userService.register(user);
    }

    // 跳转登录页面
    @GetMapping("/login")
    public String toLogin() {
        return "pages/front/login"; // 对应 login.html
    }

    // 处理登录请求
    @PostMapping("/doLogin")
    @ResponseBody
    public String doLogin(@RequestParam String username, @RequestParam String password) {
        return userService.login(username, password);
    }

}
