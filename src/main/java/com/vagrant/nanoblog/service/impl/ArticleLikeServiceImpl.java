package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.ArticleLike;
import com.vagrant.nanoblog.mapper.ArticleLikeMapper;
import com.vagrant.nanoblog.service.IArticleLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

}
