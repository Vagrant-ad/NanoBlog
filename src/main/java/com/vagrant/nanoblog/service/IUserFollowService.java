package com.vagrant.nanoblog.service;

import com.vagrant.nanoblog.pojo.UserFollow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vagrant.nanoblog.vo.UserFollowVO;

import java.util.List;

/**
 * <p>
 * 用户关注表 服务类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface IUserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     * @param followingId 被关注者ID
     * @param followerId 关注者ID
     */
    void follow(Long followingId, Long followerId);

    /**
     * 取消关注
     * @param followingId 被关注者ID
     * @param followerId 关注者ID
     */
    void unfollow(Long followingId, Long followerId);

    /**
     * 查询是否已关注
     * @param followingId 被关注者ID
     * @param followerId 关注者ID
     * @return 是否已关注
     */
    Boolean isFollowing(Long followingId, Long followerId);

    /**
     * 获取粉丝列表（关注我的人）
     * @param userId 用户ID
     * @param currentUserId 当前登录用户ID（用于判断isFollowing）
     * @return 粉丝列表
     */
    List<UserFollowVO> getFansList(Long userId, Long currentUserId);

    /**
     * 获取关注列表（我关注的人）
     * @param userId 用户ID
     * @param currentUserId 当前登录用户ID（用于判断isFollowing）
     * @return 关注列表
     */
    List<UserFollowVO> getFollowingList(Long userId, Long currentUserId);

}
