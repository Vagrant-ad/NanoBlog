package com.vagrant.nanoblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.pojo.UserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    int insert(UserRole userRole);
}