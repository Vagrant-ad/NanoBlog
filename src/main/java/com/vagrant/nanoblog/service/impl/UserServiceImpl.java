package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.UserUpdateDTO;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.mapper.CommentMapper;
import com.vagrant.nanoblog.mapper.UserRoleMapper;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.pojo.Comment;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.mapper.UserMapper;
import com.vagrant.nanoblog.pojo.UserRole;
import com.vagrant.nanoblog.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    // key: 用户名, value: 失败次数
    private static final java.util.Map<String, Integer> failCountMap = new java.util.concurrent.ConcurrentHashMap<>();
    // key: 用户名, value: 锁定结束的时间戳（毫秒）
    private static final java.util.Map<String, Long> lockMap = new java.util.concurrent.ConcurrentHashMap<>();


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult register(User user, Long roleId) {
        //防止null
        if (user == null || user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            return ResponseResult.errorResult(400, "注册失败：密码不能为空");
        }

        // 查重：用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (this.baseMapper.selectOne(wrapper) != null) {
            return ResponseResult.errorResult(400,"该用户名已被占用");
        }

        // 密码加密
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));

        // 设置默认状态
        user.setStatus(1);

        //  执行插入
        int rows = this.baseMapper.insert(user);
        if (rows > 0) {

            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());

            userRole.setRoleId(roleId != null ? roleId : 1L);
            userRole.setCreateTime(LocalDateTime.now());

            // 插入关联表
            userRoleMapper.insert(userRole);
            return ResponseResult.okResult();
        }
        return ResponseResult.errorResult(500,"注册失败，请稍后再试");
    }


    @Override
     public   ResponseResult login(String username, String password){
        // 1. 检查是否处于锁定状态
        if (lockMap.containsKey(username)) {
            long lockTime = lockMap.get(username);
            if (System.currentTimeMillis() < lockTime) {
                // 计算剩余分钟
                long minutesLeft = (lockTime - System.currentTimeMillis()) / 1000 / 60;
                return ResponseResult.errorResult(403,"账号被锁定，请在 " + (minutesLeft + 1) + " 分钟后再试");
            } else {
                // 时间已到，自动移除锁定
                lockMap.remove(username);
                failCountMap.remove(username);
            }
        }

        // 2. 查找用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = this.getOne(wrapper);

        if (user == null) {
            return ResponseResult.errorResult(404,"用户不存在");
        }

        //3.判断账号是否被注销
        if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
            return ResponseResult.errorResult(403,"该账号已被注销！");
        }

        //4.判断账户是否被封禁
        if (user.getStatus() != null && user.getStatus() == 0) {
            return ResponseResult.errorResult(403,"您的账号已被管理员封禁！");
        }

        // 5. 校验密码
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            // 登录成功，清除该用户的失败记录
            failCountMap.remove(username);
            // 登录成功时，更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            this.updateById(user);

            return ResponseResult.okResult(user);
        } else {
            // 4. 密码错误，累计次数
            int count = failCountMap.getOrDefault(username, 0) + 1;
            failCountMap.put(username, count);

            if (count >= 5) {
                // 锁定 10 分钟
                lockMap.put(username, System.currentTimeMillis() + 600000);
                return ResponseResult.errorResult(403,"连续输错5次密码，账号已锁定10分钟");
            }
            return ResponseResult.errorResult(400,"密码错误，还可以尝试 " + (5 - count) + " 次");
        }
    }

    @Override
    public ResponseResult updateUserProfile(UserUpdateDTO dto) {
        User user = this.getById(dto.getId());
        if (user == null) return ResponseResult.errorResult(404,"用户不存在");

        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setUpdateTime(java.time.LocalDateTime.now()); // 更新时间

        boolean success = this.updateById(user);
        return success ? ResponseResult.okResult() : ResponseResult.errorResult(500,"更新失败");
    }

    @Override
    public ResponseResult updatePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        User user = this.getById(userId);
        if (user == null) return ResponseResult.errorResult(404, "用户不存在");

        // 2. 校验旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            return ResponseResult.errorResult(400, "原密码输入错误");
        }

        // 3. 设置新密码
        user.setPasswordHash(encoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());

        // 4. 更新数据库
        if (this.updateById(user)) {
            return ResponseResult.okResult("密码修改成功");
        }
        return ResponseResult.errorResult(500, "服务器异常，修改失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult deleteAccount(Long userId, String password) {
        // 1. 查询用户
        User user = this.getById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            return ResponseResult.errorResult(404, "用户不存在");
        }

        // 2. 校验密码
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            return ResponseResult.errorResult(400, "密码错误，注销失败");
        }

        // 3. 软删除：将 is_deleted 设为 1
        user.setIsDeleted(1);
        user.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(user);
        if (success) {
            return ResponseResult.okResult();
        }
        return ResponseResult.errorResult(500, "注销失败，请稍后再试");
    }

    // ===================== 【后台管理相关方法实现】 =====================
    
    @Override
    public IPage<User> getUserList(Integer page, Integer size, String username) {
        Page<User> userPage = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0); // 只查未删除的用户
        
        // 支持按用户名搜索
        if (StringUtils.hasText(username)) {
            wrapper.like("username", username);
        }
        
        wrapper.orderByDesc("create_time");
        return this.page(userPage, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserByAdmin(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setIsDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
    }

}
