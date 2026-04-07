-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: nanoblog
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `author_id` bigint unsigned NOT NULL COMMENT '作者ID',
  `category_id` bigint unsigned DEFAULT NULL COMMENT '分类ID',
  `article_title` varchar(200) NOT NULL COMMENT '标题',
  `article_slug` varchar(200) DEFAULT NULL COMMENT '文章标识',
  `article_summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `cover_image_url` varchar(255) DEFAULT NULL COMMENT '封面图',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0草稿 1发布 2归档)',
  `is_top` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶',
  `is_featured` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐',
  `view_count` bigint unsigned NOT NULL DEFAULT '0' COMMENT '浏览量',
  `like_count` bigint unsigned NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` bigint unsigned NOT NULL DEFAULT '0' COMMENT '评论数',
  `favorite_count` bigint unsigned NOT NULL DEFAULT '0' COMMENT '收藏数',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_slug` (`article_slug`),
  KEY `idx_article_author` (`author_id`),
  KEY `idx_article_category` (`category_id`),
  KEY `idx_article_status_time` (`status`,`publish_time`),
  CONSTRAINT `fk_article_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (3,2,1,'测试文章',NULL,'摘要',NULL,1,0,0,0,0,0,0,'2026-03-22 00:27:22','2026-03-22 00:27:22','2026-03-22 00:27:22',0),(4,2,1,'测试文章',NULL,'摘要',NULL,1,0,0,0,0,0,0,'2026-03-22 00:28:05','2026-03-22 00:28:05','2026-03-22 00:28:05',0),(5,2,1,'测试文章',NULL,'摘要',NULL,1,0,0,0,0,0,0,'2026-03-22 00:32:02','2026-03-22 00:32:02','2026-03-22 00:32:02',0);
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article_content`
--

DROP TABLE IF EXISTS `article_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_content` (
  `article_id` bigint unsigned NOT NULL COMMENT '文章ID',
  `content_md` longtext NOT NULL COMMENT 'Markdown内容',
  `content_html` longtext COMMENT 'HTML内容',
  PRIMARY KEY (`article_id`),
  CONSTRAINT `fk_article_content_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章内容表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article_content`
--

LOCK TABLES `article_content` WRITE;
/*!40000 ALTER TABLE `article_content` DISABLE KEYS */;
INSERT INTO `article_content` VALUES (3,'# 标题\n这是内容','<h1>标题</h1>\n<p>这是内容</p>\n'),(4,'# 标题\n这是内容','<h1>标题</h1>\n<p>这是内容</p>\n'),(5,'# 标题\n这是内容','<h1>标题</h1>\n<p>这是内容</p>\n');
/*!40000 ALTER TABLE `article_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article_favorite`
--

DROP TABLE IF EXISTS `article_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_favorite` (
  `user_id` bigint unsigned NOT NULL,
  `article_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`article_id`),
  KEY `fk_fav_article` (`article_id`),
  CONSTRAINT `fk_fav_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article_favorite`
--

LOCK TABLES `article_favorite` WRITE;
/*!40000 ALTER TABLE `article_favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `article_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article_like`
--

DROP TABLE IF EXISTS `article_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_like` (
  `user_id` bigint unsigned NOT NULL,
  `article_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`article_id`),
  KEY `fk_like_article` (`article_id`),
  CONSTRAINT `fk_like_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article_like`
--

LOCK TABLES `article_like` WRITE;
/*!40000 ALTER TABLE `article_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `article_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article_tag`
--

DROP TABLE IF EXISTS `article_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_tag` (
  `article_id` bigint unsigned NOT NULL,
  `tag_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`,`tag_id`),
  KEY `fk_article_tag_tag` (`tag_id`),
  CONSTRAINT `fk_article_tag_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_article_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article_tag`
--

LOCK TABLES `article_tag` WRITE;
/*!40000 ALTER TABLE `article_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `article_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attachment`
--

DROP TABLE IF EXISTS `attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attachment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `uploader_id` bigint unsigned NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_url` varchar(500) NOT NULL,
  `file_type` varchar(100) DEFAULT NULL,
  `file_size` bigint unsigned DEFAULT NULL,
  `file_md5` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_attachment_uploader` (`uploader_id`),
  CONSTRAINT `fk_attachment_user` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attachment`
--

LOCK TABLES `attachment` WRITE;
/*!40000 ALTER TABLE `attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` bigint unsigned DEFAULT '0' COMMENT '父分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `category_slug` varchar(100) NOT NULL COMMENT '分类标识',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_slug` (`category_slug`),
  KEY `idx_category_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,0,'前端开发','frontend',1,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(2,0,'后端开发','backend',2,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(3,0,'数据库','database',3,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(4,0,'运维部署','devops',4,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(5,0,'测试开发','test-dev',5,0,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(6,1,'Vue.js','vuejs',1,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(7,1,'React','react',2,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(8,1,'CSS3','css3',3,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(9,2,'Java','java',1,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(10,2,'Python','python',2,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(11,2,'Go','go',3,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(12,3,'MySQL','mysql',1,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(13,3,'Redis','redis',2,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(14,3,'MongoDB','mongodb',3,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(15,4,'Docker','docker',1,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0),(16,4,'K8s','k8s',2,1,'2026-03-22 00:27:02','2026-03-22 00:27:02',0);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `article_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `parent_id` bigint unsigned DEFAULT '0' COMMENT '父评论ID',
  `reply_to_user_id` bigint unsigned DEFAULT NULL COMMENT '回复用户ID',
  `comment_content` varchar(2000) NOT NULL COMMENT '评论内容',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `like_count` bigint unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_comment_article_time` (`article_id`,`create_time`),
  KEY `idx_comment_user` (`user_id`),
  KEY `idx_comment_parent` (`parent_id`),
  CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_like`
--

DROP TABLE IF EXISTS `comment_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_like` (
  `user_id` bigint unsigned NOT NULL,
  `comment_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`comment_id`),
  KEY `fk_comment_like_comment` (`comment_id`),
  CONSTRAINT `fk_comment_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`),
  CONSTRAINT `fk_comment_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_like`
--

LOCK TABLES `comment_like` WRITE;
/*!40000 ALTER TABLE `comment_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` (`role_code`, `role_name`, `remark`, `create_time`, `update_time`, `is_deleted`)
VALUES
    ('USER',  '普通用户', '默认注册角色，可发布文章、评论', NOW(), NOW(), 0),
    ('ADMIN', '管理员',   '拥有后台全部管理权限',          NOW(), NOW(), 0);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL,
  `config_value` text,
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(50) NOT NULL COMMENT '标签名',
  `tag_slug` varchar(100) NOT NULL COMMENT '标签标识',
  `tag_color` varchar(20) DEFAULT NULL COMMENT '标签颜色',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`),
  UNIQUE KEY `uk_tag_slug` (`tag_slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `nickname` varchar(50) NOT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(1正常 0禁用)',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除(0否 1是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (2,'test_user_01','$2a$10$7JB720yubVSbXyM9sL0ey.3lZ84Mq8v5y895G9sL0ey7JB720yubVS','测试用户01',NULL,NULL,NULL,1,NULL,'2026-03-22 00:19:05','2026-03-22 00:19:05',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_follow`
--

DROP TABLE IF EXISTS `user_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `follower_id` bigint unsigned NOT NULL COMMENT '关注者',
  `following_id` bigint unsigned NOT NULL COMMENT '被关注者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follower_id`,`following_id`),
  KEY `fk_follow_target` (`following_id`),
  CONSTRAINT `fk_follow_target` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_follow_user` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_follow`
--

LOCK TABLES `user_follow` WRITE;
/*!40000 ALTER TABLE `user_follow` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_follow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_user_role_role` (`role_id`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-24 20:54:53
