package com.vagrant.nanoblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.pojo.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vagrant.nanoblog.vo.CommentManageVO;
import com.vagrant.nanoblog.vo.CommentVO;
import java.util.List;
/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface ICommentService extends IService<Comment> {
    List<CommentVO> getCommentTree(Long articleId);
    void addComment(Long articleId, Long userId, String content, Long parentId);
    void deleteComment(Long commentId, Long userId);
    
    // ===================== 【后台管理相关方法】 =====================
    /** 管理员视角的全量评论列表，支持按 articleId 筛选 */
    IPage<CommentManageVO> getAdminCommentList(Integer page, Integer size, Long articleId);
    
    /** 管理员删除评论（软删除），同时 article.comment_count -1 */
    void deleteCommentByAdmin(Long commentId);
}
