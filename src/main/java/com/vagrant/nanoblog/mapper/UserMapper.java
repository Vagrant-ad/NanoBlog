package com.vagrant.nanoblog.mapper;

import com.vagrant.nanoblog.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface UserMapper extends BaseMapper<User> {
    int insert(User user);
}
