package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.UserFollow;
import com.vagrant.nanoblog.mapper.UserFollowMapper;
import com.vagrant.nanoblog.service.IUserFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户关注表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements IUserFollowService {

}
