# Changelog

所有重要的变更都会记录在这个文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added
- 认知卡片完整功能实现
  - 一句话导读、认知冲突标记、原文引用
  - 脑图节点树结构 (MindMapNode)
  - 原文内容分段存储 (SourceContent)
  - 冲突检测服务 (ConflictDetectionService)
  - 脑图服务 (MindMapService)
  - 原文段落提取服务 (SourceContentService)
  - CardController 8个API端点
- 功能任务清单扩展至70个功能点
- 项目文档完善
  - README.md
  - .gitignore
  - LICENSE
  - CONTRIBUTING.md
  - CHANGELOG.md
- 数据库初始化脚本 (init.sql)
- Docker 部署配置
  - Dockerfile.backend
  - docker-compose.yml
  - nginx.conf
- 环境变量模板 (.env.example)
- 单元测试示例

## [0.2.0] - 2026-02-14

### Added
- 用户注册与登录系统 (feat_003)
  - 手机号验证码登录/注册
  - 微信 OAuth 登录
  - JWT Token 管理
  - 短信验证码服务（腾讯云SMS + 模拟模式）
- 核心业务实体类和 Service 层
  - 7个实体类：Card, Source, Task, PlanCatalog, SubscriptionOrder, UserSubscription, AiCallLog
  - 7个 Repository 接口
  - 5个 Service 接口及实现

### Security
- Spring Security 配置
- JWT 认证过滤器
- 密码加密存储

## [0.1.0] - 2026-02-14

### Added
- 项目初始化
  - Android 项目结构 (Kotlin + Jetpack Compose)
  - Spring Boot 后端项目结构
  - Git 仓库配置
- Android 基础架构
  - 主题系统（冷色调极简主义）
  - 导航框架（底部5Tab）
  - 基础页面（欢迎页、登录页、首页等）
- 后端基础架构
  - 统一响应格式
  - 全局异常处理
  - Swagger API 文档
  - 跨域配置

### Infrastructure
- CI/CD 基础配置
- 数据库设计文档
- API 接口设计文档

---

**版本说明：**
- 版本号格式：MAJOR.MINOR.PATCH
  - MAJOR：不兼容的 API 修改
  - MINOR：向下兼容的功能新增
  - PATCH：向下兼容的问题修复
