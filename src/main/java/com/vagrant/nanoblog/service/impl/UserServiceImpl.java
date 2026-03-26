package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.mapper.UserMapper;
import com.vagrant.nanoblog.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
}
