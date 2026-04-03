package com.vagrant.nanoblog.controller;

import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.pojo.ArticleContent;
import com.vagrant.nanoblog.service.IArticleService;
import com.vagrant.nanoblog.service.IArticleContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
/**
 * 文章编辑与发布控制器
 */
@RestController
@RequestMapping("/admin/article")
public class ArticleEditorController {

    @Autowired
    private IArticleService articleService;

    @Autowired
    private IArticleContentService articleContentService;

    /**
     * 根据 ID 获取文章详情（包含内容）
     * GET /admin/article/{id}
     */
    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> getArticleById(@PathVariable Long id) {
        try {
            Article article = articleService.getById(id);
            if (article == null) {
                return ResponseResult.errorResult(404, "文章不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", article.getId());
            result.put("articleTitle", article.getArticleTitle());
            result.put("categoryId", article.getCategoryId());
            result.put("articleSummary", article.getArticleSummary());
            result.put("coverImageUrl", article.getCoverImageUrl());
            result.put("status", article.getStatus());
            result.put("isTop", article.getIsTop());
            result.put("isFeatured", article.getIsFeatured());
            result.put("authorId", article.getAuthorId());

            // 获取文章内容
            ArticleContent content = articleContentService.getByArticleId(id);
            if (content != null) {
                result.put("content", content.getContentMd());
                result.put("contentHtml", content.getContentHtml());
            }

            return ResponseResult.okResult(result);
        } catch (Exception e) {
            return ResponseResult.errorResult(500, "获取文章详情失败：" + e.getMessage());
        }
    }

    /**
     * 发布新文章
     * POST /admin/article/publish
     */
    @PostMapping("/publish")
    public ResponseResult<Article> publishArticle(@RequestBody ArticlePublishDTO dto) {
        try {
            Article article = articleService.publishArticle(dto);
            return ResponseResult.okResult(article);
        } catch (Exception e) {
            return ResponseResult.errorResult(500, "发布文章失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章
     * PUT /admin/article/update
     */
    @PutMapping("/update")
    public ResponseResult<Article> updateArticle(@RequestBody ArticlePublishDTO dto) {
        try {
            if (dto.getId() == null) {
                return ResponseResult.errorResult(400, "文章 ID 不能为空");
            }

            Article article = articleService.updateArticle(dto);
            return ResponseResult.okResult(article);
        } catch (Exception e) {
            return ResponseResult.errorResult(500, "更新文章失败：" + e.getMessage());
        }
    }

    /**
     * 上传图片（可选功能）
     * POST /admin/article/upload/image
     */
    @PostMapping("/upload/image")
    @ResponseBody
    public Map<String, Object> uploadImage(@RequestParam("editormd-image-file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("success", 0);
                result.put("message", "上传文件为空");
                return result;
            }

            // TODO: 实现文件上传逻辑，保存到本地或 OSS
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String imageUrl = "/uploads/" + fileName;

            // 保存文件到服务器
            // file.transferTo(new File(uploadPath, fileName));

            result.put("success", 1);
            result.put("message", "上传成功");
            result.put("url", imageUrl);

            return result;
        } catch (Exception e) {
            result.put("success", 0);
            result.put("message", "上传失败：" + e.getMessage());
            return result;
        }
    }
}
