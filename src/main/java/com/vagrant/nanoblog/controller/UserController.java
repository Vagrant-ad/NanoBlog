package com.vagrant.nanoblog.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.dto.UserUpdateDTO;
import com.vagrant.nanoblog.mapper.UserRoleMapper;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.pojo.UserRole;
import com.vagrant.nanoblog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
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

    @Autowired
    private UserRoleMapper userRoleMapper;

    // 跳转到注册页面
    @GetMapping("/register")
    public String toRegister() {
        return "register"; // 对应register.html
    }

    // 处理注册请求
    @PostMapping("/doRegister")
    @ResponseBody
    public ResponseResult doRegister(@RequestBody UserRegisterDTO dto) {
        // 1. 手动将 DTO 里的值赋给 User 实体类
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());

        // 把 DTO 的 password 赋值给 User 的 passwordHash
        user.setPasswordHash(dto.getPassword());

        // 2. 调用 service
        return userService.register(user, dto.getRoleId());
    }

    // 跳转登录页面
    @GetMapping("/login")
    public String toLogin() {
        return "pages/front/login"; // 对应 login.html
    }

    //处理登录请求
    @PostMapping("/doLogin")
    @ResponseBody
    public ResponseResult doLogin(@RequestParam String username, @RequestParam String password,HttpSession session,String captcha, HttpServletRequest request) {
        // 1. 从 session 获取正确的验证码
        String sessionCaptcha = (String) request.getSession().getAttribute("captcha_key");

        // 2. 校验（忽略大小写对比）
        if (captcha == null || !captcha.equalsIgnoreCase(sessionCaptcha)) {
            return ResponseResult.errorResult(400, "验证码错误！");
        }

        // 3. 校验通过后，立即删除 session 里的验证码
        request.getSession().removeAttribute("captcha_key");

        ResponseResult result = userService.login(username, password);
        if (result.getCode() == 200) {
            // 登录成功，将整个用户对象存入 Session
            session.setAttribute("LOGIN_USER", result.getData());
        }
        return result;
    }
    //退出登录
    @PostMapping("/logout")
    @ResponseBody
    public ResponseResult logout(HttpSession session) {
        session.removeAttribute("LOGIN_USER");
        session.invalidate();
        return ResponseResult.okResult();
    }
    //验证码
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
        try {
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "No-cache");

            // 生成4位随机验证码
            String captchaText = java.util.UUID.randomUUID().toString().substring(0, 4);
            request.getSession().setAttribute("captcha_key", captchaText);

            // 创建画布
            int width = 110, height = 44;
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics g = image.getGraphics();

            // 画背景
            g.setColor(new java.awt.Color(240, 245, 248));
            g.fillRect(0, 0, width, height);

            // 画干扰线
            g.setColor(new java.awt.Color(163, 216, 224));
            for(int i=0; i<5; i++) g.drawLine((int)(Math.random()*width), (int)(Math.random()*height), (int)(Math.random()*width), (int)(Math.random()*height));

            // 画文字
            g.setColor(new java.awt.Color(61, 115, 152));
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 26));
            g.drawString(captchaText, 25, 32);

            g.dispose();
            javax.imageio.ImageIO.write(image, "JPEG", response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //获取个人信息
    @GetMapping("/getProfile")
    @ResponseBody
    public ResponseResult getProfile(HttpSession session) {
        // 1. 从 Session 获取当前登录用户
        User sessionUser = (User) session.getAttribute("LOGIN_USER");
        if (sessionUser == null) {
            return ResponseResult.errorResult(401, "请先登录");
        }

        // 2. 查询完整的用户信息
        User user = userService.getById(sessionUser.getId());

        // 3. 使用 UserRoleMapper 查询该用户的角色记录
        UserRole userRole = userRoleMapper.selectOne(
                new QueryWrapper<UserRole>().eq("user_id", user.getId())
        );


        // 4. 将数据封装进 Map
        Map<String, Object> result = new HashMap<>();
        result.put("user", user); // 放入用户基本信息
        result.put("roleId", userRole != null ? userRole.getRoleId() : 1); // 放入角色ID，默认1

        return ResponseResult.okResult(result);
    }


    /**
     * 1. 更新/完善个人信息
     */
    @PostMapping("/updateProfile")
    @ResponseBody
    public ResponseResult updateProfile(@RequestBody UserUpdateDTO updateDTO) {
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


            String imageUrl = "/user/showAvatar?name=" + newFileName;
            return ResponseResult.okResult(imageUrl);
        } catch (Exception e) {
            return ResponseResult.errorResult(500,"上传失败");
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


            response.setContentType("image/jpeg");
            java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/updatePassword")
    @ResponseBody
    public ResponseResult updatePassword(@RequestBody Map<String, String> params, HttpSession session) {
        // 1. 从 Session 获取当前登录用户
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) return ResponseResult.errorResult(401, "请先登录");

        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");

        // 2. 基础校验
        if (oldPwd == null || newPwd == null || newPwd.length() < 6) {
            return ResponseResult.errorResult(400, "密码长度不符合要求");
        }

        // 3. 调用 Service
        return userService.updatePassword(loginUser.getId(), oldPwd, newPwd);
    }

}
