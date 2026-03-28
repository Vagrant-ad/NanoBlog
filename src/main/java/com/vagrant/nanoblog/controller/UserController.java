package com.vagrant.nanoblog.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.dto.UserUpdateDTO;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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
    public ResponseResult doRegister(User user) {
        return userService.register(user);
    }

    // 跳转登录页面
    @GetMapping("/login")
    public String toLogin() {
        return "pages/front/login"; // 对应 login.html
    }

    //处理登录请求
    @PostMapping("/doLogin")
    @ResponseBody
    public ResponseResult doLogin(@RequestParam String username, @RequestParam String password,HttpSession session) {
        ResponseResult result = userService.login(username, password);
        if (result.getCode() == 200) {
            // 登录成功，将整个用户对象存入 Session
            session.setAttribute("LOGIN_USER", result.getData());
        }
        return result;
    }



    @GetMapping("/getProfile")
    @ResponseBody
    public ResponseResult getProfile(HttpSession session) {
        // 从 Session 中获取用户信息
        User user = (User) session.getAttribute("LOGIN_USER");

        if (user == null) {
            return ResponseResult.errorResult("登录已失效，请重新登录");
        }
        // 建议重新从数据库查一次，确保获取到最新的 createTime, updateTime 等信息
        User currentUser = userService.getById(user.getId());
        return ResponseResult.okResult(currentUser);
    }

    /**
     * 1. 更新/完善个人信息
     */
    @PostMapping("/updateProfile")
    @ResponseBody
    public ResponseResult updateProfile(@RequestBody UserUpdateDTO updateDTO) {
        // 这里的逻辑在 UserServiceImpl 中实现
        return userService.updateUserProfile(updateDTO);
    }

    /**
     * 2.接收前端上传的头像图片，并返回图片访问URL
     */
    @PostMapping("/uploadAvatar")
    @ResponseBody
    public ResponseResult uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + extension;

            // 使用电脑硬盘上的固定目录
            String savePath = "D:/nanoblog_uploads/";
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();

            File serverFile = new File(dir, newFileName);
            file.transferTo(serverFile);

            // 返回给前端的 URL 依然保持这个格式
            String imageUrl = "/NanoBlog_war/user/showAvatar?name=" + newFileName;
            return ResponseResult.okResult(imageUrl);
        } catch (Exception e) {
            return ResponseResult.errorResult("上传失败");
        }
    }

    /**
     * 3. 新增：读取并展示头像的接口
     * 浏览器访问这个接口，Java会去D盘读文件并返回给浏览器
     */
    @GetMapping("/showAvatar")
    public void showAvatar(@RequestParam("name") String name, javax.servlet.http.HttpServletResponse response) {
        try {
            File file = new File("D:/nanoblog_uploads/" + name);
            if (!file.exists()) return;

            // 设置响应头，告诉浏览器这是图片
            response.setContentType("image/jpeg");
            java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
