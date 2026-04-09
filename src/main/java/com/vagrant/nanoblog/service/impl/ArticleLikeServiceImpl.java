package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.pojo.ArticleLike;
import com.vagrant.nanoblog.mapper.ArticleLikeMapper;
import com.vagrant.nanoblog.service.IArticleLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章点赞表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike> implements IArticleLikeService {

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public void likeArticle(Long articleId, Long userId) {
        // 检查是否已点赞
        ArticleLike existing = articleLikeMapper.selectOne(
            new QueryWrapper<ArticleLike>()
                .eq("article_id", articleId)
                .eq("user_id", userId)
        );
        if (existing != null) {
            throw new RuntimeException("已经点赞过了");
        }

        // 插入点赞记录
        ArticleLike like = new ArticleLike();
        like.setArticleId(articleId);
        like.setUserId(userId);
        articleLikeMapper.insert(like);

        // like_count + 1
        articleMapper.update(null, new UpdateWrapper<Article>()
            .eq("id", articleId)
            .setSql("like_count = like_count + 1"));
    }

    @Override
    public void unlikeArticle(Long articleId, Long userId) {
        // 删除点赞记录
        int deleted = articleLikeMapper.delete(
            new QueryWrapper<ArticleLike>()
                .eq("article_id", articleId)
                .eq("user_id", userId)
        );

        if (deleted > 0) {
            // like_count - 1，注意不能小于 0
            articleMapper.update(null, new UpdateWrapper<Article>()
                .eq("id", articleId)
                .gt("like_count", 0)
                .setSql("like_count = like_count - 1"));
        }
    }

    @Override
    public Boolean isLiked(Long articleId, Long userId) {
        ArticleLike existing = articleLikeMapper.selectOne(
            new QueryWrapper<ArticleLike>()
                .eq("article_id", articleId)
                .eq("user_id", userId)
        );
        return existing != null;
    }
}
