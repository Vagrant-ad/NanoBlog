package com.vagrant.nanoblog.service;

import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.UserRegisterDTO;
import com.vagrant.nanoblog.dto.UserUpdateDTO;
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
    ResponseResult register(User user, Long roleId);
    ResponseResult login(String username, String password); //登录方法
    // 更新个人资料：使用 DTO 接收参数
    ResponseResult updateUserProfile(UserUpdateDTO dto);

    // 添加方法定义
    ResponseResult updatePassword(Long userId, String oldPassword, String newPassword);


}



