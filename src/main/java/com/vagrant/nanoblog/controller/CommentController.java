package com.vagrant.nanoblog.controller;

import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.ICommentService;
import com.vagrant.nanoblog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    /**
     * 获取当前登录用户 ID
     */
    private Long getCurrentUserId(HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            throw new RuntimeException("请先登录");
        }
        return loginUser.getId();
    }

    /**
     * 查询文章评论（树形结构）
     * GET /comment/list/{articleId}
     */
    @GetMapping("/list/{articleId}")
    public ResponseResult<List<CommentVO>> list(@PathVariable Long articleId) {
        List<CommentVO> comments = commentService.getCommentTree(articleId);
        return ResponseResult.okResult(comments);
    }

    /**
     * 发表评论
     * POST /comment/add
     * 参数：{
     *   "articleId": 1,
     *   "commentContent": "这是评论内容",
     *   "parentId": 0  (可选，根评论传 0 或不传，子评论传父评论 ID)
     *   "replyToUserId: 0
     * }
     */
    @PostMapping("/add")
    public ResponseResult<Void> add(
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        Long userId = getCurrentUserId(session);
        
        Long articleId = Long.valueOf(params.get("articleId").toString());
        String content = (String) params.get("commentContent");
        Long parentId = params.get("parentId") != null ? 
                         Long.valueOf(params.get("parentId").toString()) : 0;
        Long replyToUserId = params.get("replyToUserId") != null
                ? Long.valueOf(params.get("replyToUserId").toString()) : null;
        commentService.addComment(articleId, userId, content, parentId,replyToUserId);
        return ResponseResult.okResult();
    }

    /**
     * 软删除评论
     * DELETE /comment/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id, HttpSession session) {
        Long userId = getCurrentUserId(session);
        commentService.deleteComment(id, userId);
        return ResponseResult.okResult();
    }
}
