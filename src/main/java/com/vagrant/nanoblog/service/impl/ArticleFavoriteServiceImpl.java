package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.ArticleFavorite;
import com.vagrant.nanoblog.mapper.ArticleFavoriteMapper;
import com.vagrant.nanoblog.service.IArticleFavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章收藏表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class ArticleFavoriteServiceImpl extends ServiceImpl<ArticleFavoriteMapper, ArticleFavorite> implements IArticleFavoriteService {

}
