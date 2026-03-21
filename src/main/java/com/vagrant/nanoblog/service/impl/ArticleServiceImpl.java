package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.service.IArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

}
