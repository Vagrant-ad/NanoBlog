package com.vagrant.nanoblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    // ===================== 【后台管理相关方法】 =====================
    /** 分页查询用户列表，支持按用户名搜索 */
    IPage<User> getUserList(Integer page, Integer size, String username);
    
    /** 切换用户状态（0禁用/1启用） */
    void toggleUserStatus(Long userId, Integer status);
    
    /** 管理员软删除用户 */
    void deleteUserByAdmin(Long userId);
}



