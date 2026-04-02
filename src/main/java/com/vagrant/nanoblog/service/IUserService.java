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
    ResponseResult login(String username, String password);
    // 更新个人资料
    ResponseResult updateUserProfile(UserUpdateDTO dto);

    // 更新密码
    ResponseResult updatePassword(Long userId, String oldPassword, String newPassword);
    // 注销账号（软删除）
    ResponseResult deleteAccount(Long userId, String password);


}



