package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vagrant.nanoblog.pojo.Comment;
import com.vagrant.nanoblog.mapper.CommentMapper;
import com.vagrant.nanoblog.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.mapper.UserMapper;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.vo.CommentManageVO;
import com.vagrant.nanoblog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<CommentVO> getCommentTree(Long articleId) {
        //查根评论（parent_id = 0）
        List<Comment> roots = this.list(
                new QueryWrapper<Comment>()
                        .eq("article_id", articleId)
                        .eq("parent_id", 0)
                        .eq("is_deleted", 0)
                        .orderByAsc("create_time")
        );

        //查所有回复（parent_id != 0）
        List<Comment> replies = this.list(
                new QueryWrapper<Comment>()
                        .eq("article_id", articleId)
                        .ne("parent_id", 0)
                        .eq("is_deleted", 0)
                        .orderByAsc("create_time")
        );

        //收集所有用户ID，批量查询用户信息
        Set<Long> userIds = new HashSet<>();
        roots.forEach(c -> userIds.add(c.getUserId()));
        replies.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) userIds.add(c.getReplyToUserId());
        });

        Map<Long, User> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        } else {
            userMap = new HashMap<>();
        }

        //按parentId分组回复
        Map<Long, List<CommentVO>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(
                        Comment::getParentId,
                        Collectors.mapping((Comment c) -> toVO(c, userMap), Collectors.toList())
                ));

        //组装树形结构
        return roots.stream().map(r -> {
            CommentVO vo = toVO(r, userMap);
            vo.setReplies(replyMap.getOrDefault(r.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Long articleId, Long userId, String content, Long parentId, Long replyToUserId) {
        //写入评论
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setCommentContent(content);
        comment.setParentId(parentId != null ? parentId : 0);
        comment.setReplyToUserId(replyToUserId);
        comment.setStatus(1);
        comment.setLikeCount(0L);
        this.save(comment);

        //文章评论数 +1
        articleMapper.update(null,
                new UpdateWrapper<Article>()
                        .eq("id", articleId)
                        .setSql("comment_count = comment_count + 1")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = this.getById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        // 软删除
        comment.setIsDeleted(1);
        this.updateById(comment);
        //如果是父评论删除所有子评论
        if (comment.getParentId() == null || comment.getParentId() == 0L) {
            //查出所有子评论
            List<Comment> children = this.list(
                    new QueryWrapper<Comment>()
                            .eq("parent_id", commentId)
                            .eq("is_deleted", 0)
            );
            if (!children.isEmpty()) {
                //批量软删除
                children.forEach(c -> c.setIsDeleted(1));
                this.updateBatchById(children);

                //评论数减去1+children.size()
                long deleteCount = 1L + children.size();
                articleMapper.update(null,
                        new UpdateWrapper<Article>()
                                .eq("id", comment.getArticleId())
                                .setSql("comment_count = comment_count - " + deleteCount)
                );
                return; //直接返回
            }
        }

        //无子评论情况文章评论数 -1
        articleMapper.update(null,
                new UpdateWrapper<Article>()
                        .eq("id", comment.getArticleId())
                        .setSql("comment_count = comment_count - 1")
        );
    }

    /**
     * Comment 转 CommentVO
     */
    private CommentVO toVO(Comment comment, Map<Long, User> userMap) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setCommentContent(comment.getCommentContent());
        vo.setCreateTime(comment.getCreateTime());
        vo.setLikeCount(comment.getLikeCount());
        vo.setReplyToUserId(comment.getReplyToUserId());

        User user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
        }
        //查回复人昵称
        if (comment.getReplyToUserId() != null) {
            User replyTo = userMap.get(comment.getReplyToUserId());
            if (replyTo != null) vo.setReplyToNickname(replyTo.getNickname());
        }
        return vo;
    }

    // ===================== 【后台管理相关方法实现】 =====================
    
    @Override
    public IPage<CommentManageVO> getAdminCommentList(Integer page, Integer size, Long articleId) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 10;
        
        Page<Comment> commentPage = new Page<>(page, size);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0); // 只查未删除的
        
        // 支持按文章 ID 筛选
        if (articleId != null) {
            queryWrapper.eq("article_id", articleId);
        }
        
        queryWrapper.orderByDesc("create_time");
        IPage<Comment> commentPageResult = this.page(commentPage, queryWrapper);
        
        // 转换为 VO（需要 JOIN user 和 article 表）
        Page<CommentManageVO> result = new Page<>();
        result.setTotal(commentPage.getTotal());
        result.setCurrent(commentPage.getCurrent());
        result.setSize(commentPage.getSize());
        
        if (commentPageResult.getRecords().isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }
        
        // 批量查询用户信息和文章信息
        Set<Long> userIds = commentPageResult.getRecords().stream()
                .map(Comment::getUserId).collect(Collectors.toSet());
        Set<Long> articleIds = commentPageResult.getRecords().stream()
                .map(Comment::getArticleId).collect(Collectors.toSet());
        
        final Map<Long, User> userMapFinal;
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }
        userMapFinal = userMap;
        
        final Map<Long, Article> articleMapFinal;
        Map<Long, Article> articleMap = new HashMap<>();
        if (!articleIds.isEmpty()) {
            List<Article> articles = articleMapper.selectBatchIds(articleIds);
            articleMap = articles.stream().collect(Collectors.toMap(Article::getId, a -> a));
        }
        articleMapFinal = articleMap;
        
        List<CommentManageVO> voList = commentPageResult.getRecords().stream().map(c -> {
            CommentManageVO vo = new CommentManageVO();
            vo.setId(c.getId());
            vo.setCommentContent(c.getCommentContent());
            vo.setCreateTime(c.getCreateTime());
            vo.setStatus(c.getStatus());
            vo.setArticleId(c.getArticleId());
            
            // 设置评论者昵称
            User user = userMapFinal.get(c.getUserId());
            if (user != null) {
                vo.setAuthorNickname(user.getNickname());
            }
            
            // 设置文章标题
            Article article = articleMapFinal.get(c.getArticleId());
            if (article != null) {
                vo.setArticleTitle(article.getArticleTitle());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        result.setRecords(voList);
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new RuntimeException("评论不存在");
        }
        
        // 软删除
        comment.setIsDeleted(1);
        this.updateById(comment);
        
        // 文章评论数 -1
        articleMapper.update(null,
            new UpdateWrapper<Article>()
                .eq("id", comment.getArticleId())
                .setSql("comment_count = comment_count - 1")
        );
    }
}
