# EvoMind 贡献指南

感谢您对 EvoMind 项目的关注！我们欢迎所有形式的贡献，包括但不限于：
- 报告 Bug
- 提交功能建议
- 改进文档
- 提交代码修复或新功能

## 开发环境搭建

### 前端 (Android)

1. 安装 Android Studio Hedgehog (2023.1.1) 或更高版本
2. 配置 JDK 17
3. 克隆项目并导入 `android-app` 目录
4. 同步 Gradle 依赖

### 后端

1. 安装 JDK 17
2. 安装 Maven 3.9+
3. 安装 MySQL 8.0 和 Redis 7.0
4. 执行数据库初始化脚本 `database/init.sql`

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

## 代码规范

### Kotlin (Android)

- 使用 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- 使用 Compose 函数式编程风格
- ViewModel 使用 `viewModelScope` 管理协程
- 使用 Hilt 进行依赖注入

### Java (后端)

- 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 使用 Lombok 简化代码
- Controller 层使用 Swagger 注解
- Service 层添加事务注解

### Git 提交规范

提交信息格式：`<type>(<scope>): <subject>`

**Type 类型：**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例：**
```
feat(card): 添加认知冲突检测功能
fix(auth): 修复登录时 token 过期问题
docs(api): 更新支付接口文档
```

## 分支策略

- `main`: 生产分支，只接受来自 `develop` 的合并
- `develop`: 开发分支，功能分支合并目标
- `feature/*`: 功能分支，从 `develop` 创建
- `hotfix/*`: 紧急修复分支，从 `main` 创建

## Pull Request 流程

1. 从 `develop` 分支创建新的功能分支
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. 进行开发并提交代码
   ```bash
   git add .
   git commit -m "feat: 添加新功能"
   git push origin feature/your-feature-name
   ```

3. 在 GitHub 上创建 Pull Request
   - 目标分支选择 `develop`
   - 填写详细的 PR 描述
   - 关联相关的 Issue

4. 等待代码审查
   - 至少需要 1 个审查者批准
   - 解决审查意见

5. 合并后删除功能分支

## 测试要求

### 单元测试

- 核心逻辑必须有单元测试覆盖
- 测试覆盖率目标：> 70%

```bash
# Android
./gradlew test

# Backend
mvn test
```

### 集成测试

- API 接口需要集成测试
- 数据库操作需要测试

## 文档更新

- 新功能必须包含文档更新
- API 变更需要更新 `docs/` 目录下的文档
- 复杂功能需要添加使用示例

## 问题报告

### Bug 报告

使用 GitHub Issues 提交 Bug，请包含：
- 问题描述
- 复现步骤
- 期望行为
- 实际行为
- 环境信息（设备、系统版本、App 版本）
- 截图（如适用）

### 功能建议

- 清晰描述功能需求
- 说明使用场景
- 如有参考，提供类似产品/功能示例

## 行为准则

- 保持友好和尊重
- 欢迎新手贡献者
- 建设性的反馈
- 专注于解决问题

## 联系方式

- Issue: https://github.com/duruifeng01-pixel/EvoMind-2.0/issues
- Discussion: https://github.com/duruifeng01-pixel/EvoMind-2.0/discussions

再次感谢您的贡献！
