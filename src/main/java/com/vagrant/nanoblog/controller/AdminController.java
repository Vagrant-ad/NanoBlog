package com.vagrant.nanoblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.Category;
import com.vagrant.nanoblog.pojo.Tag;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.service.ICategoryService;
import com.vagrant.nanoblog.service.ICommentService;
import com.vagrant.nanoblog.service.ITagService;
import com.vagrant.nanoblog.service.IUserService;
import com.vagrant.nanoblog.vo.ArticleManageVO;
import com.vagrant.nanoblog.vo.CommentManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理控制器
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;
    private final IArticleService articleService;
    private final ICommentService commentService;
    private final ICategoryService categoryService;
    private final ITagService tagService;

    //仪表盘统计接口

    /**
     * 获取统计数据
     * GET /admin/stats
     */
    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        //用户总数
        long userCount = userService.lambdaQuery().eq(User::getIsDeleted, 0).count();
        stats.put("userCount", userCount);

        //文章总数
        long articleCount = articleService.lambdaQuery().eq(com.vagrant.nanoblog.pojo.Article::getIsDeleted, 0).count();
        stats.put("articleCount", articleCount);

        //评论总数
        long commentCount = commentService.lambdaQuery().eq(com.vagrant.nanoblog.pojo.Comment::getIsDeleted, 0).count();
        stats.put("commentCount", commentCount);

        //今日新增文章
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayArticleCount = articleService.lambdaQuery()
                .eq(com.vagrant.nanoblog.pojo.Article::getIsDeleted, 0)
                .ge(com.vagrant.nanoblog.pojo.Article::getCreateTime, todayStart)
                .count();
        stats.put("todayArticleCount", todayArticleCount);

        return ResponseResult.okResult(stats);
    }

    //用户管理接口

    /**
     * 分页查询用户列表
     * GET /admin/user/list?page=1&size=10&username=xxx
     */
    @GetMapping("/user/list")
    public ResponseResult<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {
        return ResponseResult.okResult(userService.getUserList(page, size, username));
    }

    /**
     * 切换用户状态
     * PUT /admin/user/{id}/status?status=0
     */
    @PutMapping("/user/{id}/status")
    public ResponseResult<Void> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.toggleUserStatus(id, status);
        return ResponseResult.okResult();
    }

    /**
     * 删除用户
     * DELETE /admin/user/{id}
     */
    @DeleteMapping("/user/{id}")
    public ResponseResult<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserByAdmin(id);
        return ResponseResult.okResult();
    }

    //文章管理接口

    /**
     * 管理员视角的文章列表
     * GET /admin/article/list?page=1&size=10&status=1&title=xxx
     */
    @GetMapping("/article/list")
    public ResponseResult<IPage<ArticleManageVO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String title) {
        return ResponseResult.okResult(articleService.getAdminArticleList(page, size, status, title));
    }

    /**
     * 修改文章状态
     * PUT /admin/article/{id}/status?status=2
     */
    @PutMapping("/article/{id}/status")
    public ResponseResult<Void> updateArticleStatus(@PathVariable Long id, @RequestParam Integer status) {
        articleService.updateArticleStatusByAdmin(id, status);
        return ResponseResult.okResult();
    }

    /**
     * 删除文章
     * DELETE /admin/article/{id}
     */
    @DeleteMapping("/article/{id}")
    public ResponseResult<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticleByAdmin(id);
        return ResponseResult.okResult();
    }

    //评论管理接口

    /**
     * 管理员视角的评论列表
     * GET /admin/comment/list?page=1&size=10&articleId=xxx
     */
    @GetMapping("/comment/list")
    public ResponseResult<IPage<CommentManageVO>> getCommentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long articleId) {
        return ResponseResult.okResult(commentService.getAdminCommentList(page, size, articleId));
    }

    /**
     * 删除评论
     * DELETE /admin/comment/{id}
     */
    @DeleteMapping("/comment/{id}")
    public ResponseResult<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteCommentByAdmin(id);
        return ResponseResult.okResult();
    }

    //分类管理接口

    /**
     * 新增分类
     * POST /admin/category/add
     */
    @PostMapping("/category/add")
    public ResponseResult<Void> addCategory(@RequestBody Category category) {
        categoryService.addCategory(category);
        return ResponseResult.okResult();
    }

    /**
     * 修改分类
     * PUT /admin/category/{id}
     */
    @PutMapping("/category/{id}")
    public ResponseResult<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateCategory(category);
        return ResponseResult.okResult();
    }

    /**
     * 删除分类
     * DELETE /admin/category/{id}
     */
    @DeleteMapping("/category/{id}")
    public ResponseResult<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryByAdmin(id);
        return ResponseResult.okResult();
    }

    //标签管理接口

    /**
     * 新增标签
     * POST /admin/tag/add
     */
    @PostMapping("/tag/add")
    public ResponseResult<Void> addTag(@RequestBody Tag tag) {
        tagService.addTag(tag);
        return ResponseResult.okResult();
    }

    /**
     * 删除标签
     * DELETE /admin/tag/{id}
     */
    @DeleteMapping("/tag/{id}")
    public ResponseResult<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTagByAdmin(id);
        return ResponseResult.okResult();
    }
}
