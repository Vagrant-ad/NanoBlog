package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.mapper.UserMapper;
import com.vagrant.nanoblog.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

}
