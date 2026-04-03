package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.mapper.CommentMapper;
import com.vagrant.nanoblog.mapper.UserMapper;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.pojo.Comment;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vagrant.nanoblog.vo.CommentManageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ... existing code ...

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    // ... existing code ...

    @Override
    public IPage<CommentManageVO> getAllComments(Integer pageNum, Integer pageSize, Long articleId, String articleTitle) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 100) pageSize = 10;

        Page<Comment> pageInfo = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0); // 只查询未删除的评论

        // 按文章 ID 筛选
        if (articleId != null) {
            queryWrapper.eq("article_id", articleId);
        }

        // 按文章标题模糊搜索（需要关联查询）
        if (StringUtils.hasText(articleTitle)) {
            // 先查询符合条件的文章 ID
            QueryWrapper<Article> articleWrapper = new QueryWrapper<>();
            articleWrapper.like("article_title", articleTitle);
            articleWrapper.eq("is_deleted", 0);
            List<Article> articles = articleMapper.selectList(articleWrapper);

            if (!articles.isEmpty()) {
                List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());
                queryWrapper.in("article_id", articleIds);
            } else {
                // 如果没有匹配的文章，返回空结果
                Page<CommentManageVO> emptyResult = new Page<>();
                emptyResult.setTotal(0);
                emptyResult.setCurrent(pageNum);
                emptyResult.setSize(pageSize);
                emptyResult.setRecords(Collections.emptyList());
                return emptyResult;
            }
        }


        // 按创建时间降序
        queryWrapper.orderByDesc("create_time");

        IPage<Comment> commentPage = this.page(pageInfo, queryWrapper);
        return convertToManageVO(commentPage);
    }

    @Override
    public void adminDeleteComment(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 软删除
        comment.setStatus(1); // 1=已删除
        comment.setUpdateTime(java.time.LocalDateTime.now());
        this.updateById(comment);
    }

    @Override
    public List<CommentManageVO> getRecentComments(Integer limit) {
        if (limit == null || limit < 1) limit = 5;
        if (limit > 20) limit = 20;

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0)
                .eq("status", 0) // 只查询正常状态的评论
                .orderByDesc("create_time")
                .last("LIMIT " + limit);

        List<Comment> comments = this.list(queryWrapper);

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询文章信息
        List<Long> articleIds = comments.stream()
                .map(Comment::getArticleId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, Article> articleMap;
        if (!articleIds.isEmpty()) {
            List<Article> articles = articleMapper.selectBatchIds(articleIds);
            articleMap = articles.stream().collect(Collectors.toMap(Article::getId, a -> a));
        } else {
            articleMap = Collections.emptyMap();
        }

        // 批量查询用户信息
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, User> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        } else {
            userMap = Collections.emptyMap();
        }

        // 转换为 VO
        return comments.stream().map(c -> {
            CommentManageVO vo = new CommentManageVO();
            vo.setId(c.getId());
            vo.setArticleId(c.getArticleId());
            vo.setUserId(c.getUserId());
            vo.setCommentContent(c.getCommentContent());
            vo.setStatus(c.getStatus());
            vo.setCreateTime(c.getCreateTime());

            // 设置文章标题
            Article article = articleMap.get(c.getArticleId());
            if (article != null) {
                vo.setArticleTitle(article.getArticleTitle());
            }

            // 设置评论者昵称
            User user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setAuthorNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 转换为 ManageVO
     */
    private IPage<CommentManageVO> convertToManageVO(IPage<Comment> commentPage) {
        Page<CommentManageVO> result = new Page<>();
        result.setTotal(commentPage.getTotal());
        result.setCurrent(commentPage.getCurrent());
        result.setSize(commentPage.getSize());

        if (commentPage.getRecords().isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 批量查询文章信息
        List<Long> articleIds = commentPage.getRecords().stream()
                .map(Comment::getArticleId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, Article> articleMap;
        if (!articleIds.isEmpty()) {
            List<Article> articles = articleMapper.selectBatchIds(articleIds);
            articleMap = articles.stream().collect(Collectors.toMap(Article::getId, a -> a));
        } else {
            articleMap = Collections.emptyMap();
        }

        // 批量查询用户信息
        List<Long> userIds = commentPage.getRecords().stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, User> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        } else {
            userMap = Collections.emptyMap();
        }

        List<CommentManageVO> voList = commentPage.getRecords().stream().map(c -> {
            CommentManageVO vo = new CommentManageVO();
            vo.setId(c.getId());
            vo.setArticleId(c.getArticleId());
            vo.setUserId(c.getUserId());
            vo.setCommentContent(c.getCommentContent());
            vo.setStatus(c.getStatus());
            vo.setCreateTime(c.getCreateTime());

            // 设置文章标题
            Article article = articleMap.get(c.getArticleId());
            if (article != null) {
                vo.setArticleTitle(article.getArticleTitle());
            }

            // 设置评论者昵称
            User user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setAuthorNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }

            return vo;
        }).collect(Collectors.toList());

        result.setRecords(voList);
        return result;
    }
}
