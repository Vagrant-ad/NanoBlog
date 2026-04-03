package com.vagrant.nanoblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.vo.ArticleManageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 后台文章管理控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@RestController
@RequestMapping("/admin/article")

public class AdminArticleController {

    @Autowired
    private IArticleService articleService;

    /**
     * 分页查询文章列表（支持状态和标题筛选）
     * GET /admin/article/list
     */
    @GetMapping("/list")
    public ResponseResult getArticleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String title) {

        IPage<ArticleManageVO> result = articleService.getAllArticles(pageNum, pageSize, status, title);

        return ResponseResult.okResult(result);
    }

    /**
     * 修改文章状态
     * PUT /admin/article/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseResult updateArticleStatus(
            @PathVariable Long id,
            @RequestBody ArticleStatusDTO dto) {

        try {
            articleService.updateArticleStatus(id, dto.getStatus());
            return ResponseResult.okResult();
        } catch (Exception e) {
            return ResponseResult.errorResult(500, e.getMessage());
        }
    }

    /**
     * 删除文章（软删除）
     * DELETE /admin/article/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseResult deleteArticle(@PathVariable Long id) {
        try {
            articleService.adminDeleteArticle(id);
            return ResponseResult.okResult();
        } catch (Exception e) {
            return ResponseResult.errorResult(500, e.getMessage());
        }
    }

    /**
     * 文章状态 DTO
     */
    public static class ArticleStatusDTO {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

}
