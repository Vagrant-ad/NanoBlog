package com.vagrant.nanoblog.mapper;

import com.vagrant.nanoblog.pojo.UserFollow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vagrant.nanoblog.vo.UserFollowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户关注表 Mapper 接口
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    // 查询粉丝列表（关注我的人）
    List<UserFollowVO> getFansList(@Param("userId") Long userId);

    // 查询关注列表（我关注的人）
    List<UserFollowVO> getFollowingList(@Param("userId") Long userId);

}
