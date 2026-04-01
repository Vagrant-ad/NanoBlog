package com.vagrant.nanoblog.service.impl;
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
        // 1. 查根评论（parent_id = 0）
        List<Comment> roots = this.list(
            new QueryWrapper<Comment>()
                .eq("article_id", articleId)
                .eq("parent_id", 0)
                .eq("is_deleted", 0)
                .orderByAsc("create_time")
        );

        // 2. 查所有回复（parent_id != 0）
        List<Comment> replies = this.list(
            new QueryWrapper<Comment>()
                .eq("article_id", articleId)
                .ne("parent_id", 0)
                .eq("is_deleted", 0)
        );

        // 3. 收集所有用户 ID，批量查询用户信息（避免 N+1 问题）
        Set<Long> userIds = new HashSet<>();
        roots.forEach(c -> userIds.add(c.getUserId()));
        replies.forEach(c -> userIds.add(c.getUserId()));

        Map<Long, User> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        } else {
            userMap = new HashMap<>();
        }

        // 4. 按 parentId 分组回复 - 修复 lambda 表达式变量问题
        Map<Long, List<CommentVO>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(
                    Comment::getParentId,
                    Collectors.mapping((Comment c) -> toVO(c, userMap), Collectors.toList())
                ));

        // 5. 组装树形结构
        return roots.stream().map(r -> {
            CommentVO vo = toVO(r, userMap);
            vo.setReplies(replyMap.getOrDefault(r.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Long articleId, Long userId, String content, Long parentId) {
        // 1. 写入评论
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setCommentContent(content);
        comment.setParentId(parentId != null ? parentId : 0);
        comment.setStatus(1);
        comment.setLikeCount(0L);
        this.save(comment);

        // 2. 文章评论数 +1
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

        // 文章评论数 -1
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
        vo.setCommentContent(comment.getCommentContent());
        vo.setCreateTime(comment.getCreateTime());
        vo.setLikeCount(comment.getLikeCount());

        User user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
        }

        return vo;
    }
}
