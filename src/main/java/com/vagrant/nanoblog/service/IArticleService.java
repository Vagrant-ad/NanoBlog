package com.vagrant.nanoblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vagrant.nanoblog.dto.ArticlePublishDTO;
import com.vagrant.nanoblog.pojo.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vagrant.nanoblog.vo.ArticleDetailVO;
import com.vagrant.nanoblog.vo.ArticleHomeVO;
import com.vagrant.nanoblog.vo.ArticleListVO;
import com.vagrant.nanoblog.vo.ArticleManageVO;

import java.util.List;

/**
 * <p>
 * 文章表 服务类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface IArticleService extends IService<Article> {
    //文章发布与主页文章列表相关
    Long publishArticle(ArticlePublishDTO dto, Long userId);
    void saveArticleTags(Long articleId, List<String> tagNames);
    IPage<ArticleListVO> getArticleList(Integer page, Integer size);
    ArticleDetailVO getArticleDetail(Long id);
    // 修改后的方法（加上categoryId）
    IPage<ArticleHomeVO> getHomeArticleList(Integer page, Integer size, String keyword, String sortBy, Long categoryId,Long tagId);
    //个人中心相关
    /** 查询某用户的已发布文章列表 */
    IPage<ArticleManageVO> getMyPublished(Long userId, Integer page, Integer size);
    /** 查询某用户的草稿列表 */
    IPage<ArticleManageVO> getMyDrafts(Long userId, Integer page, Integer size);
    /** 更新文章（编辑已发布/草稿均走此方法） */
    void updateArticle(Long articleId, ArticlePublishDTO dto, Long userId);
    /** 软删除文章 */
    void deleteArticle(Long articleId, Long userId);
    /** 草稿直接发布 */
    void publishDraft(Long articleId, Long userId);

    IPage<ArticleListVO> listByCategory(Long categoryId, Integer pageNum, Integer pageSize);

    // ===================== 【新增：按标签查询文章】 =====================
    IPage<ArticleHomeVO> listByTag(Long tagId, Integer pageNum, Integer pageSize);

    // ===================== 【后台管理相关方法】 =====================
    /** 管理员视角的全量文章列表，支持按状态/标题筛选 */
    IPage<ArticleManageVO> getAdminArticleList(Integer page, Integer size, Integer status, String title);
    
    /** 管理员修改文章状态（下架改为 status=2 归档） */
    void updateArticleStatusByAdmin(Long articleId, Integer status);
    
    /** 管理员删除文章（跳过归属校验） */
    void deleteArticleByAdmin(Long articleId);
}