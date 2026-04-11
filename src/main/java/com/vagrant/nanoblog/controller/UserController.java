package com.vagrant.nanoblog.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.dto.UserUpdateDTO;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.mapper.UserFollowMapper;
import com.vagrant.nanoblog.mapper.UserRoleMapper;
import com.vagrant.nanoblog.pojo.Attachment;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.pojo.UserFollow;
import com.vagrant.nanoblog.pojo.UserRole;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.service.ICommentService;
import com.vagrant.nanoblog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.service.ICommentService;


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
    @Autowired
    private AttachmentController attachmentController; //注入附件控制器

    @Autowired
    private IArticleService articleService;

    @Autowired
    private ICommentService commentService;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private ArticleMapper articleMapper;

    //跳转到注册页面
    @GetMapping("/register")
    public String toRegister() {
        return "register"; //对应register.html
    }

    //处理注册请求
    @PostMapping("/doRegister")
    @ResponseBody
    public ResponseResult doRegister(@RequestBody UserRegisterDTO dto) {
        //1. 手动将DTO里的值赋给User实体类
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());

        //把DTO的password赋值给User的passwordHash
        user.setPasswordHash(dto.getPassword());

        //2. 调用service
        return userService.register(user, dto.getRoleId());
    }

    //跳转登录页面
    @GetMapping("/login")
    public String toLogin() {
        return "pages/front/login"; //对应login.html
    }

    //处理登录请求
    @PostMapping("/doLogin")
    @ResponseBody
    public ResponseResult doLogin(@RequestParam String username, @RequestParam String password,HttpSession session,String captcha, HttpServletRequest request) {
        //1. 从 session 获取正确的验证码
        String sessionCaptcha = (String) request.getSession().getAttribute("captcha_key");

        //2. 校验（忽略大小写对比）
        if (captcha == null || !captcha.equalsIgnoreCase(sessionCaptcha)) {
            return ResponseResult.errorResult(400, "验证码错误！");
        }

        //3. 校验通过后，立即删除 session 里的验证码
        request.getSession().removeAttribute("captcha_key");

        ResponseResult result = userService.login(username, password);
        if (result.getCode() == 200) {
            //登录成功，将整个用户对象存入Session
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

            //生成4位随机验证码
            String captchaText = java.util.UUID.randomUUID().toString().substring(0, 4);
            request.getSession().setAttribute("captcha_key", captchaText);

            //创建画布
            int width = 110, height = 44;
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics g = image.getGraphics();

            //画背景
            g.setColor(new java.awt.Color(240, 245, 248));
            g.fillRect(0, 0, width, height);

            //画干扰线
            g.setColor(new java.awt.Color(163, 216, 224));
            for(int i=0; i<5; i++) g.drawLine((int)(Math.random()*width), (int)(Math.random()*height), (int)(Math.random()*width), (int)(Math.random()*height));

            //画文字
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
        //1. 从Session获取当前登录用户
        User sessionUser = (User) session.getAttribute("LOGIN_USER");
        if (sessionUser == null) {
            return ResponseResult.errorResult(401, "请先登录");
        }

        //2. 查询完整的用户信息
        User user = userService.getById(sessionUser.getId());

        //3. 使用UserRoleMapper查询该用户的角色记录
        UserRole userRole = userRoleMapper.selectOne(
                new QueryWrapper<UserRole>().eq("user_id", user.getId())
        );

        //4. 将数据封装进Map
        Map<String, Object> result = new HashMap<>();
        result.put("user", user); //放入用户基本信息
        result.put("roleId", userRole != null ? userRole.getRoleId() : 1); //放入角色ID，默认1

        return ResponseResult.okResult(result);
    }


    //更新/完善个人信息
    @PostMapping("/updateProfile")
    @ResponseBody
    public ResponseResult updateProfile(@RequestBody UserUpdateDTO updateDTO) {
        return userService.updateUserProfile(updateDTO);
    }


    /**
     * 上传头像 (直接复用 AttachmentController 的逻辑)
     */
    @PostMapping("/uploadAvatar")
    @ResponseBody
    public ResponseResult uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            HttpSession session) {


        return attachmentController.uploadImage(file, request, session);
    }


    //修改密码
    @PostMapping("/updatePassword")
    @ResponseBody
    public ResponseResult updatePassword(@RequestBody Map<String, String> params, HttpSession session) {
        //1. 从Session获取当前登录用户
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) return ResponseResult.errorResult(401, "请先登录");

        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");

        //2. 基础校验
        if (oldPwd == null || newPwd == null || newPwd.length() < 6) {
            return ResponseResult.errorResult(400, "密码长度不符合要求");
        }

        //3. 调用Service
        return userService.updatePassword(loginUser.getId(), oldPwd, newPwd);
    }

    //删除账号
    @PostMapping("/deleteAccount")
    @ResponseBody
    public ResponseResult deleteAccount(@RequestBody Map<String, String> params, HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) return ResponseResult.errorResult(401, "请先登录");

        String password = params.get("password");
        if (password == null || password.trim().isEmpty()) {
            return ResponseResult.errorResult(400, "请输入密码");
        }

        ResponseResult result = userService.deleteAccount(loginUser.getId(), password);
        if (result.getCode() == 200) {
            //注销成功后清除Session
            session.removeAttribute("LOGIN_USER");
            session.invalidate();
        }
        return result;
    }

    /**
     * 获取用户公开资料（访客可访问，不返回敏感字段）
     * GET /user/publicProfile?id=xxx
     */
    @GetMapping("/publicProfile")
    @ResponseBody
    public ResponseResult getPublicProfile(@RequestParam Long id) {
        User user = userService.getById(id);
        if (user == null || user.getIsDeleted() == 1) {
            return ResponseResult.errorResult(404, "用户不存在");
        }
        //脱敏：清除密码哈希
        user.setPasswordHash(null);

        UserRole userRole = userRoleMapper.selectOne(
                new QueryWrapper<UserRole>().eq("user_id", id)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roleId", userRole != null ? userRole.getRoleId() : 1);
        return ResponseResult.okResult(result);
    }

    /**
     * 获取用户统计信息（评论数、总浏览量、关注数、粉丝数、获赞数）
     * GET /user/getStats?userId=xxx
     * userId 可选：不传则查当前登录用户，传则查目标用户（访客模式）
     */
    @GetMapping("/getStats")
    @ResponseBody
    public ResponseResult getStats(@RequestParam(required = false) Long userId, HttpSession session) {
        Long targetId = userId;
        if (targetId == null) {
            User loginUser = (User) session.getAttribute("LOGIN_USER");
            if (loginUser == null) return ResponseResult.errorResult(401, "请先登录");
            targetId = loginUser.getId();
        }

        //总浏览量：查该用户所有文章的view_count之和
        Long totalView = articleService.lambdaQuery()
                .eq(com.vagrant.nanoblog.pojo.Article::getAuthorId, targetId)
                .eq(com.vagrant.nanoblog.pojo.Article::getIsDeleted, 0)
                .list()
                .stream()
                .mapToLong(a -> a.getViewCount() == null ? 0L : a.getViewCount())
                .sum();

        //评论数：查该用户发出的评论总数
        long commentCount = commentService.lambdaQuery()
                .eq(com.vagrant.nanoblog.pojo.Comment::getUserId, targetId)
                .eq(com.vagrant.nanoblog.pojo.Comment::getIsDeleted, 0)
                .count();

        //关注数：我关注了多少人
        long followingCount = userFollowMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserFollow>()
                        .eq("follower_id", targetId));

        //粉丝数：多少人关注了我
        long fansCount = userFollowMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserFollow>()
                        .eq("following_id", targetId));

        //文章获赞：该用户所有文章的like_count求和
        Long articleLike = articleMapper.sumLikeCountByAuthor(targetId);
        long totalArticleLike = articleLike != null ? articleLike : 0L;

        //评论获赞：查该用户所有发出的评论被点赞的数量总和
        long totalCommentLike = commentService.lambdaQuery()
                .eq(com.vagrant.nanoblog.pojo.Comment::getUserId, targetId)
                .eq(com.vagrant.nanoblog.pojo.Comment::getIsDeleted, 0)
                .list()
                .stream()
                .mapToLong(c -> c.getLikeCount() == null ? 0L : c.getLikeCount())
                .sum();
        //最终总获赞数
        long totalLike = totalArticleLike + totalCommentLike;

        Map<String, Object> stats = new HashMap<>();
        stats.put("viewCount", totalView);
        stats.put("commentCount", commentCount);
        stats.put("followCount", followingCount);
        stats.put("fansCount", fansCount);
        stats.put("likeCount", totalLike);

        return ResponseResult.okResult(stats);
    }

}
