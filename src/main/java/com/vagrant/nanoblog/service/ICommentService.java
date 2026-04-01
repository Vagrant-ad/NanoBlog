package com.vagrant.nanoblog.service;

import com.vagrant.nanoblog.pojo.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
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
}
