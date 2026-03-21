package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.Comment;
import com.vagrant.nanoblog.mapper.CommentMapper;
import com.vagrant.nanoblog.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

}
