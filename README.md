# NanoBlog

一个基于 **SSM（Spring MVC + Spring + MyBatis-Plus）** 的轻量级技术博客平台，支持 Markdown 富文本写作、多角色管理、评论互动与用户社交等功能。

---

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [功能特性](#功能特性)
- [项目结构](#项目结构)
- [数据库设计](#数据库设计)
- [快速开始](#快速开始)
- [接口概览](#接口概览)
- [页面预览](#页面预览)

---

## 项目简介

NanoBlog 是一个面向开发者的技术博客社区，用户可以注册登录、发布 Markdown 文章、对文章和评论点赞、关注其他用户。平台内置后台管理系统，管理员可对用户、文章、评论、分类与标签进行全量管理。

---

## 技术栈

### 后端

| 技术 | 说明 |
|---|---|
| Spring MVC | Web 层框架，处理请求路由与拦截器 |
| MyBatis-Plus | ORM 框架，简化 CRUD 与分页操作 |
| Druid | 数据库连接池 |
| BCrypt | 密码加密 |
| CommonMark | 服务端 Markdown → HTML 渲染 |
| Thymeleaf | 模板引擎（部分页面） |
| Session | 登录态管理 |

### 前端

| 技术 | 说明 |
|---|---|
| Layui | UI 组件库（表格、分页、弹层等） |
| EasyMDE | Markdown 编辑器 |
| Highlight.js | 代码块语法高亮 |
| KaTeX | 数学公式渲染 |
| Font Awesome | 图标库 |
| 原生 Fetch API | 前后端 AJAX 通信 |

### 数据库

- **MySQL 8.x**

---

## 功能特性

### 前台

- **文章**：Markdown 写作、草稿保存、一键发布、分类/标签筛选、浏览量统计
- **首页**：双列卡片流，支持按时间/浏览量/点赞数排序，关键词全文搜索
- **文章详情**：目录自动生成、代码高亮复制、数学公式、任务列表、Admonition 提示块
- **评论**：树形评论（根评论 + 回复），评论点赞，软删除联动文章评论数
- **点赞**：文章点赞 / 取消点赞，评论点赞 / 取消点赞
- **用户中心**：个人资料编辑、头像上传、密码修改、账号注销
- **社交**：关注 / 取消关注、粉丝列表、关注列表
- **标签页**：标签云展示，按热度/名称排序，关键词搜索

### 后台（仅管理员）

- **仪表盘**：用户总数、文章总数、评论总数、今日新增文章
- **用户管理**：分页查询、禁用/启用账号、软删除
- **文章管理**：分页查询、按状态/标题筛选、归档、删除
- **评论管理**：按文章 ID 筛选、删除
- **分类管理**：树形展示（父子两级）、新增/编辑/删除
- **标签管理**：新增/删除

---

## 项目结构

```
nanoblog/
├── sql/
│   ├── nanoblog-master.sql          # 完整建表 + 初始数据
│   └── comment_like.sql             # 评论点赞表
├── src/main/
│   ├── java/com/vagrant/nanoblog/
│   │   ├── common/                  # 统一响应封装 ResponseResult
│   │   ├── config/                  # WebMvcConfig（拦截器注册）
│   │   ├── controller/              # 各业务 Controller
│   │   ├── dto/                     # 请求数据传输对象
│   │   ├── handler/                 # MyBatis 类型处理器
│   │   ├── interceptor/             # AdminInterceptor 后台鉴权
│   │   ├── mapper/                  # MyBatis-Plus Mapper 接口
│   │   ├── pojo/                    # 实体类
│   │   ├── service/                 # Service 接口 + 实现
│   │   └── vo/                      # 视图对象
│   ├── resources/
│   │   ├── applicationContext.xml   # Spring 核心配置
│   │   ├── springmvc.xml            # SpringMVC 配置
│   │   ├── mybatis-config.xml       # MyBatis 配置（分页插件）
│   │   ├── jdbc.properties          # 数据库连接配置
│   │   └── com/vagrant/nanoblog/mapper/  # Mapper XML 映射文件
│   └── webapp/
│       ├── pages/
│       │   ├── admin/               # 后台管理页面
│       │   └── front/               # 前台页面
│       └── static/
│           ├── css/                 # 样式文件
│           ├── js/                  # 前端逻辑
│           ├── images/              # 静态图片资源
│           └── lib/layui/           # Layui 组件库
```

---

## 数据库设计

| 表名 | 说明 |
|---|---|
| `user` | 用户表 |
| `user_role` | 用户角色关联表 |
| `role` | 角色表（USER / ADMIN） |
| `user_follow` | 用户关注关系表 |
| `article` | 文章主表（含统计计数） |
| `article_content` | 文章内容表（Markdown + HTML） |
| `article_tag` | 文章标签关联表 |
| `article_like` | 文章点赞表 |
| `article_favorite` | 文章收藏表 |
| `category` | 分类表（支持父子两级） |
| `tag` | 标签表 |
| `comment` | 评论表（树形，parent_id=0 为根评论） |
| `comment_like` | 评论点赞表 |
| `attachment` | 附件/图片上传记录表 |
| `sys_config` | 系统配置表 |

---

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.x
- Tomcat 9.x

### 部署步骤

**1. 克隆项目**

```bash
git clone https://github.com/vagrant-ad/nanoblog.git
cd nanoblog
```

**2. 初始化数据库**

```sql
CREATE DATABASE nanoblog DEFAULT CHARACTER SET utf8mb4;
USE nanoblog;

-- 执行建表脚本
SOURCE sql/nanoblog-master.sql;
```

**3. 修改数据库配置**

编辑 `src/main/resources/jdbc.properties`：

```properties
jdbc.url=jdbc:mysql://localhost:3306/nanoblog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
jdbc.username=your_username
jdbc.password=your_password
jdbc.driverClassName=com.mysql.cj.jdbc.Driver
```

**4. 配置文件上传路径**

编辑 `src/main/resources/application.properties`（或对应配置文件）：

```properties
upload.root=/your/upload/path/
upload.urlPrefix=/uploads/
```

**5. 构建并部署**

```bash
mvn clean package -DskipTests
# 将生成的 war 包部署至 Tomcat webapps 目录
```

**6. 访问**

| 地址 | 说明 |
|---|---|
| `http://localhost:8080/pages/front/index.html` | 前台首页 |
| `http://localhost:8080/pages/front/login.html` | 用户登录 |
| `http://localhost:8080/pages/admin/dashboard.html` | 后台管理（需管理员账号） |

### 默认数据

项目初始化后可注册账号，注册时选择角色：
- **普通用户（USER）**：可发文章、评论、关注
- **管理员（ADMIN）**：额外拥有后台管理权限

---

## 接口概览

### 用户

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/user/doLogin` | 登录 |
| POST | `/user/doRegister` | 注册 |
| POST | `/user/logout` | 退出登录 |
| GET | `/user/getProfile` | 获取当前登录用户信息 |
| POST | `/user/updateProfile` | 更新个人资料 |
| POST | `/user/updatePassword` | 修改密码 |
| POST | `/user/deleteAccount` | 注销账号 |

### 文章

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/article/publish` | 发布文章 |
| GET | `/article/home` | 首页文章列表（支持关键词/排序/分类/标签筛选） |
| GET | `/article/{id}` | 文章详情 |
| PUT | `/article/{id}` | 更新文章 |
| DELETE | `/article/{id}` | 删除文章 |
| POST | `/article/{id}/publish` | 草稿发布 |
| GET | `/article/my/published` | 我的已发布文章 |
| GET | `/article/my/drafts` | 我的草稿箱 |

### 评论

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/comment/list/{articleId}` | 获取文章评论树 |
| POST | `/comment/add` | 发表评论/回复 |
| DELETE | `/comment/{id}` | 删除评论 |
| POST | `/comment/like/{commentId}` | 点赞评论 |
| DELETE | `/comment/like/{commentId}` | 取消点赞评论 |

### 关注

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/follow/{userId}` | 关注用户 |
| DELETE | `/follow/{userId}` | 取消关注 |
| GET | `/follow/check/{userId}` | 查询是否已关注 |
| GET | `/follow/fans/{userId}` | 粉丝列表 |
| GET | `/follow/following/{userId}` | 关注列表 |

### 后台管理（需管理员权限）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/admin/stats` | 仪表盘统计 |
| GET | `/admin/user/list` | 用户列表 |
| PUT | `/admin/user/{id}/status` | 切换用户状态 |
| DELETE | `/admin/user/{id}` | 删除用户 |
| GET | `/admin/article/list` | 文章列表 |
| PUT | `/admin/article/{id}/status` | 修改文章状态 |
| DELETE | `/admin/article/{id}` | 删除文章 |
| GET | `/admin/comment/list` | 评论列表 |
| DELETE | `/admin/comment/{id}` | 删除评论 |
| POST | `/admin/category/add` | 新增分类 |
| PUT | `/admin/category/{id}` | 修改分类 |
| DELETE | `/admin/category/{id}` | 删除分类 |
| POST | `/admin/tag/add` | 新增标签 |
| DELETE | `/admin/tag/{id}` | 删除标签 |

---

## 页面预览

| 页面 | 路径 |
|---|---|
| 首页 | `pages/front/index.html` |
| 文章详情 | `pages/front/post.html?id={id}` |
| 写文章/编辑 | `pages/front/editor.html` |
| 个人主页 | `pages/front/profile.html?id={userId}` |
| 标签页 | `pages/front/tags.html` |
| 登录 | `pages/front/login.html` |
| 注册 | `pages/front/register.html` |
| 后台仪表盘 | `pages/admin/dashboard.html` |
| 用户管理 | `pages/admin/user.html` |
| 文章管理 | `pages/admin/post.html` |
| 评论管理 | `pages/admin/comment.html` |
| 分类标签管理 | `pages/admin/category.html` |

---