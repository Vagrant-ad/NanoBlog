package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.dto.UserUpdateDTO;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.mapper.UserMapper;
import com.vagrant.nanoblog.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    // key: 用户名, value: 失败次数
    private static final java.util.Map<String, Integer> failCountMap = new java.util.concurrent.ConcurrentHashMap<>();
    // key: 用户名, value: 锁定结束的时间戳（毫秒）
    private static final java.util.Map<String, Long> lockMap = new java.util.concurrent.ConcurrentHashMap<>();


    @Override
    public ResponseResult register(User user) {
        // 查重：用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (this.baseMapper.selectOne(wrapper) != null) {
            return ResponseResult.errorResult("该用户名已被占用"); // 使用封装类
        }

        // 密码加密
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));

        // 设置默认状态
        user.setStatus(1);

        //  执行插入
        int rows = this.baseMapper.insert(user);
        return rows > 0 ? ResponseResult.okResult() : ResponseResult.errorResult("注册失败，请稍后再试");
    }


    @Override
     public   ResponseResult login(String username, String password){
        // 1. 检查是否处于锁定状态
        if (lockMap.containsKey(username)) {
            long lockTime = lockMap.get(username);
            if (System.currentTimeMillis() < lockTime) {
                // 计算剩余分钟
                long minutesLeft = (lockTime - System.currentTimeMillis()) / 1000 / 60;
                return ResponseResult.errorResult("账号被锁定，请在 " + (minutesLeft + 1) + " 分钟后再试");
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
            return ResponseResult.errorResult("用户不存在");
        }

        //判断账户是否被封禁
        if (user.getStatus() != null && user.getStatus() == 0) {
            return ResponseResult.errorResult("您的账号已被管理员封禁！");
        }
        // 3. 校验密码
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            // 登录成功，清除该用户的失败记录
            failCountMap.remove(username);
            // 登录成功时，更新最后登录时间（可选）
            user.setLastLoginTime(LocalDateTime.now());
            this.updateById(user);

            return ResponseResult.okResult(user);//返回用户信息的成功结果
        } else {
            // 4. 密码错误，累计次数
            int count = failCountMap.getOrDefault(username, 0) + 1;
            failCountMap.put(username, count);

            if (count >= 5) {
                // 锁定 10 分钟：当前时间 + 10分钟 * 60秒 * 1000毫秒
                lockMap.put(username, System.currentTimeMillis() + 600000);
                return ResponseResult.errorResult("连续输错5次密码，账号已锁定10分钟");
            }
            return ResponseResult.errorResult("密码错误，还可以尝试 " + (5 - count) + " 次");
        }
    }

    @Override
    public ResponseResult updateUserProfile(UserUpdateDTO dto) {
        User user = this.getById(dto.getId());
        if (user == null) return ResponseResult.errorResult("用户不存在");

        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setUpdateTime(java.time.LocalDateTime.now()); // 更新时间

        boolean success = this.updateById(user);
        return success ? ResponseResult.okResult() : ResponseResult.errorResult("更新失败");
    }

}
