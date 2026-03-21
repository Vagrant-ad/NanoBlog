package com.vagrant.nanoblog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 用户关注表
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@TableName("user_follow")
public class UserFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关注者
     */
    private Long followerId;

    /**
     * 被关注者
     */
    private Long followingId;

    private LocalDateTime createTime;

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }
    public Long getFollowingId() {
        return followingId;
    }

    public void setFollowingId(Long followingId) {
        this.followingId = followingId;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserFollow{" +
            "followerId=" + followerId +
            ", followingId=" + followingId +
            ", createTime=" + createTime +
        "}";
    }
}
