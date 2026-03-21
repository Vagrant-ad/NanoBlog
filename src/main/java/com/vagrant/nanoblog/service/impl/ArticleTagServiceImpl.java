package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.ArticleTag;
import com.vagrant.nanoblog.mapper.ArticleTagMapper;
import com.vagrant.nanoblog.service.IArticleTagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章标签关联表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements IArticleTagService {

}
