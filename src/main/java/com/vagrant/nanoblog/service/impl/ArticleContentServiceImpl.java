package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.mapper.ArticleContentMapper;
import com.vagrant.nanoblog.pojo.ArticleContent;
import com.vagrant.nanoblog.service.IArticleContentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章内容表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class ArticleContentServiceImpl extends ServiceImpl<ArticleContentMapper, ArticleContent> implements IArticleContentService {

    @Override
    public ArticleContent getByArticleId(Long articleId) {
        QueryWrapper<ArticleContent> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        return this.getOne(wrapper);
    }
}
