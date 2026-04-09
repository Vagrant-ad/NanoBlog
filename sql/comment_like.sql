-- 创建评论点赞表
CREATE TABLE IF NOT EXISTS `comment_like` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`user_id`, `comment_id`),
    KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';
