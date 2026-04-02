package com.vagrant.nanoblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.service.ICommentService;
import com.vagrant.nanoblog.vo.CommentManageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台评论管理控制器
 */
@RestController
@RequestMapping("/admin/comment")
public class AdminCommentController {

    @Autowired
    private ICommentService commentService;

    /**
     * 分页查询评论列表
     * GET /admin/comment/list
     */
    @GetMapping("/list")
    public ResponseResult getCommentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String articleTitle) {

        IPage<CommentManageVO> result = commentService.getAllComments(pageNum, pageSize, articleId, articleTitle);
        return ResponseResult.okResult(result);
    }

    /**
     * 删除评论
     * DELETE /admin/comment/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseResult deleteComment(@PathVariable Long id) {
        try {
            commentService.adminDeleteComment(id);
            return ResponseResult.okResult();
        } catch (Exception e) {
            return ResponseResult.errorResult(500, e.getMessage());
        }
    }
}
