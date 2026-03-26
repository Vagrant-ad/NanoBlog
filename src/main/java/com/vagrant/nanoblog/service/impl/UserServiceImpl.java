package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.mapper.UserMapper;
import com.vagrant.nanoblog.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
    public String register(User user) {
        // 查重：用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (this.baseMapper.selectOne(wrapper) != null) {
            return "该用户名已被占用";
        }

        // 密码加密
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));

        // 设置默认状态
        user.setStatus(1);

        //  执行插入
        int rows = this.baseMapper.insert(user);
        return rows > 0 ? "success" : "注册失败，请稍后再试";
    }


    @Override
     public   String login(String username, String password){
        // 1. 检查是否处于锁定状态
        if (lockMap.containsKey(username)) {
            long lockTime = lockMap.get(username);
            if (System.currentTimeMillis() < lockTime) {
                // 计算剩余分钟
                long minutesLeft = (lockTime - System.currentTimeMillis()) / 1000 / 60;
                return "账号已被锁定，请在大约 " + (minutesLeft + 1) + " 分钟后再试";
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
            return "用户不存在";
        }

        //判断账户是否被封禁
        if (user.getStatus() != null && user.getStatus() == 0) {
            return "您的账号已被管理员封禁！";
        }
        // 3. 校验密码
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            // 登录成功，清除该用户的失败记录
            failCountMap.remove(username);
            return "success";
        } else {
            // 4. 密码错误，累计次数
            int count = failCountMap.getOrDefault(username, 0) + 1;
            failCountMap.put(username, count);

            if (count >= 5) {
                // 锁定 10 分钟：当前时间 + 10分钟 * 60秒 * 1000毫秒
                lockMap.put(username, System.currentTimeMillis() + 600000);
                return "连续输错5次密码，账号已锁定10分钟";
            }
            return "密码错误，还可以尝试 " + (5 - count) + " 次";
        }
    }

}
