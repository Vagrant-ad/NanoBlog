package com.vagrant.nanoblog.mapper;

import com.vagrant.nanoblog.pojo.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vagrant.nanoblog.vo.ArticleDetailVO;

/**
 * <p>
 * 文章表 Mapper 接口
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
public interface ArticleMapper extends BaseMapper<Article> {
    ArticleDetailVO getArticleDetailById(Long id);
    void updateViewCount(Long id);

}
