package com.vagrant.nanoblog.service;

import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface IUserService extends IService<User> {
    String register(User user);
}



