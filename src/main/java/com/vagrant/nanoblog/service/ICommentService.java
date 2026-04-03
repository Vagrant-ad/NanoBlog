package com.vagrant.nanoblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vagrant.nanoblog.vo.CommentManageVO;
/**
 * <p>
 * 评论表 服务类
 * </p>
 * @author vagrant
 * @since 2026-03-21
 */
public interface ICommentService extends IService<Comment> {
    // ... existing methods ...

    // 后台管理：分页查询评论列表
    IPage<CommentManageVO> getAllComments(Integer pageNum, Integer pageSize, Long articleId, String articleTitle);

    // 后台管理：删除评论
    void adminDeleteComment(Long commentId);

    // 获取最新评论列表（用于仪表盘）
    java.util.List<CommentManageVO> getRecentComments(Integer limit);
}
