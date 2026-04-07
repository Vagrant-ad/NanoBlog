package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.mapper.UserFollowMapper;
import com.vagrant.nanoblog.pojo.UserFollow;
import com.vagrant.nanoblog.service.IUserFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vagrant.nanoblog.vo.UserFollowVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Override
    public void follow(Long followingId, Long followerId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }
        
        UserFollow existing = userFollowMapper.selectOne(
            new QueryWrapper<UserFollow>()
                .eq("follower_id", followerId)
                .eq("following_id", followingId)
        );
        if (existing != null) {
            throw new RuntimeException("已经关注了");
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        userFollowMapper.insert(follow);
    }

    @Override
    public void unfollow(Long followingId, Long followerId) {
        userFollowMapper.delete(
            new QueryWrapper<UserFollow>()
                .eq("follower_id", followerId)
                .eq("following_id", followingId)
        );
    }

    @Override
    public Boolean isFollowing(Long followingId, Long followerId) {
        UserFollow existing = userFollowMapper.selectOne(
            new QueryWrapper<UserFollow>()
                .eq("follower_id", followerId)
                .eq("following_id", followingId)
        );
        return existing != null;
    }

    @Override
    public List<UserFollowVO> getFansList(Long userId, Long currentUserId) {
        // 查询粉丝列表
        List<UserFollowVO> fansList = userFollowMapper.getFansList(userId);
        
        // 如果当前用户已登录，批量查询是否也关注了这些人
        if (currentUserId != null && !fansList.isEmpty()) {
            List<Long> fanIds = fansList.stream()
                .map(UserFollowVO::getUserId)
                .collect(Collectors.toList());
            
            // 查询当前用户关注的人
            List<UserFollow> followings = userFollowMapper.selectList(
                new QueryWrapper<UserFollow>()
                    .eq("follower_id", currentUserId)
                    .in("following_id", fanIds)
            );
            
            Set<Long> followingSet = followings.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toSet());
            
            // 填充 isFollowing 字段
            for (UserFollowVO vo : fansList) {
                vo.setIsFollowing(followingSet.contains(vo.getUserId()));
            }
        } else {
            // 未登录，isFollowing 全部设为 false
            for (UserFollowVO vo : fansList) {
                vo.setIsFollowing(false);
            }
        }
        
        return fansList;
    }

    @Override
    public List<UserFollowVO> getFollowingList(Long userId, Long currentUserId) {
        // 查询关注列表
        List<UserFollowVO> followingList = userFollowMapper.getFollowingList(userId);
        
        // 如果当前用户已登录，批量查询是否也关注了这些人
        if (currentUserId != null && !followingList.isEmpty()) {
            List<Long> followingIds = followingList.stream()
                .map(UserFollowVO::getUserId)
                .collect(Collectors.toList());
            
            // 查询当前用户关注的人
            List<UserFollow> followings = userFollowMapper.selectList(
                new QueryWrapper<UserFollow>()
                    .eq("follower_id", currentUserId)
                    .in("following_id", followingIds)
            );
            
            Set<Long> followingSet = followings.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toSet());
            
            // 填充 isFollowing 字段
            for (UserFollowVO vo : followingList) {
                vo.setIsFollowing(followingSet.contains(vo.getUserId()));
            }
        } else {
            // 未登录，isFollowing 全部设为 false
            for (UserFollowVO vo : followingList) {
                vo.setIsFollowing(false);
            }
        }
        
        return followingList;
    }
}
