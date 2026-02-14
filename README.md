# EvoMind - 个人成长认知外骨骼

[![GitHub](https://img.shields.io/github/license/duruifeng01-pixel/EvoMind-2.0)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)

## 项目简介

EvoMind 是一款面向个人成长的认知外骨骼应用，帮助用户通过 AI 技术实现知识的内化与能力的进化。

### 核心功能

- 📸 **截图OCR导入** - 一键识别博主，快速建立信息源
- 🔗 **链接自动抓取** - 支持小红书、知乎、微信公众号等内容导入
- 🎙️ **语音快速记录** - 随时随地记录灵感
- 🤖 **AI认知卡片** - 自动提取核心观点、金句、案例
- 🧠 **知识脑图** - 可视化知识结构，支持下钻阅读
- ⚡ **认知冲突标记** - 自动识别不同观点，避免信息茧房
- 🗣️ **苏格拉底式对话** - AI引导深度思考
- 📝 **变步频挑战** - 个性化任务系统
- 💰 **透明订阅** - 算力成本一目了然

## 技术架构

### 技术栈

| 层级 | 技术 |
|------|------|
| **Android端** | Kotlin + Jetpack Compose + MVVM + Room + Hilt |
| **后端** | Spring Boot 3.x + Java 17 + MySQL + Redis |
| **AI** | 百度文心一言API + 本地ONNX Runtime |
| **第三方SDK** | 百度OCR、讯飞语音、微信支付、支付宝 |

### 项目结构

```
EvoMind/
├── android-app/          # Android客户端
│   ├── app/src/main/     # 主模块源码
│   ├── data/             # 数据层（local/remote/repository）
│   ├── domain/           # 领域层（model/usecase）
│   ├── ui/               # UI层（screens/components/theme）
│   └── service/          # 服务层（OCR/语音/AI）
├── backend/              # Spring Boot后端
│   ├── src/main/java/    # Java源码
│   ├── controller/       # API控制器
│   ├── service/          # 业务服务层
│   ├── repository/       # 数据访问层
│   ├── entity/           # 实体类
│   ├── dto/              # 数据传输对象
│   ├── security/         # 安全配置
│   └── resources/        # 配置文件
├── database/             # 数据库脚本
├── docs/                 # 项目文档
├── scripts/              # 部署脚本
└── docker/               # Docker配置
```

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- MySQL 8.0
- Redis 7.0
- Maven 3.9+

### 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Android端启动

1. 使用 Android Studio 打开 `android-app` 目录
2. 同步 Gradle 配置
3. 连接设备或启动模拟器
4. 点击 Run 按钮

## 功能开发进度

详见 [feature_list.json](feature_list.json)

| 模块 | 进度 | 状态 |
|------|------|------|
| 项目初始化 | 100% | ✅ |
| 用户系统 | 100% | ✅ |
| 认知卡片 | 40% | 🚧 |
| 信息源导入 | 0% | ⏳ |
| 讨论系统 | 0% | ⏳ |
| 支付订阅 | 0% | ⏳ |

## 开发指南

### 分支策略

- `main` - 生产分支
- `develop` - 开发分支
- `feature/*` - 功能分支
- `hotfix/*` - 紧急修复分支

### 提交规范

```
feat: 新功能
docs: 文档更新
fix: 修复bug
refactor: 重构
perf: 性能优化
test: 测试相关
chore: 构建/工具相关
```

## 文档

- [API接口文档](docs/03-api-contract.md)
- [数据库设计](docs/02-database-models.md)
- [技术蓝图](docs/08-全量技术蓝图-客户端服务端.md)
- [支付与订阅](docs/06-payment-and-subscription.md)
- [测试方案](docs/09-测试方案与用例清单.md)

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 开源协议

本项目采用 MIT 协议 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

- 项目主页：https://github.com/duruifeng01-pixel/EvoMind-2.0
- 问题反馈：https://github.com/duruifeng01-pixel/EvoMind-2.0/issues

---

**EvoMind** - 让知识真正内化，让能力持续进化
