# EvoMind 开发会话启动协议

> 每次新对话必须严格遵循此协议，确保开发方向正确、技能使用到位、架构保持清晰

---

## 🎯 协议目标

解决三个核心痛点：
1. **需求理解偏差** - 确保准确理解功能需求，避免方向错误
2. **技能利用不足** - 充分利用项目中的技能资源
3. **开发顺序混乱** - 保持全局架构视角，按依赖关系有序开发

---

## 📋 会话启动检查清单（必须逐条执行）

### Phase 1: 项目上下文重建（强制）

```markdown
- [ ] 1.1 读取开发需求文档 **AI安卓App开发需求提示词：个人成长认知外骨骼App（EvoMind）.docx**（需求权威文件）
- [ ] 1.2 读取 app_spec.txt 理解项目整体规格
- [ ] 1.3 读取 feature_list.json 了解功能清单和完成状态
- [ ] 1.4 读取 claude-progress.txt 了解开发历史和当前进度
- [ ] 1.5 检查 docs/ 目录下的架构文档
- [ ] 1.6 查看 Git 历史了解最近修改
- [ ] 1.7 确认当前数据库迁移版本
```

### Phase 2: 技能系统激活（强制）

```markdown
- [ ] 2.1 检查 everything-claude-code-main/skills/ 可用技能
- [ ] 2.2 检查 ui-ux-pro-max-skill-main/ 可用技能
- [ ] 2.3 根据当前任务选择并加载相关技能
- [ ] 2.4 阅读技能文档了解使用方式
```

### Phase 3: 依赖关系分析（强制）

```markdown
- [ ] 3.1 确认待开发功能的依赖项是否已完成
- [ ] 3.2 检查数据库实体是否已创建
- [ ] 3.3 检查 API 接口是否已定义
- [ ] 3.4 检查 Android 端数据模型是否已创建
- [ ] 3.5 如有未完成依赖，优先完成依赖项
```

### Phase 4: 功能理解确认（强制）

```markdown
- [ ] 4.1 从 feature_list.json 提取待开发功能的详细描述
- [ ] 4.2 理解该功能与已有功能的关系
- [ ] 4.3 确认功能的数据流和交互流程
- [ ] 4.4 与用户确认理解无误（如有疑问）
```

---

## 🔧 技能资源清单

### everything-claude-code 技能（位于 ../everything-claude-code-main/）

```
skills/
├── create-mcp-server/          # MCP 服务器创建
├── create-mode/                # 自定义模式创建
├── springboot-patterns/        # Spring Boot 模式
├── springboot-tdd/             # Spring Boot TDD
├── springboot-security/        # Spring Boot 安全
├── springboot-verification/    # Spring Boot 验证
├── jpa-patterns/               # JPA 模式
├── java-coding-standards/      # Java 编码标准
├── database-migrations/        # 数据库迁移
├── api-design/                 # API 设计
├── frontend-patterns/          # 前端模式
├── security-review/            # 安全审查
└── tdd-workflow/               # TDD 工作流
```

### ui-ux-pro-max 技能（位于 ../ui-ux-pro-max-skill-main/）

```
src/
├── design-systems/             # 设计系统
├── component-patterns/         # 组件模式
├── animation-techniques/       # 动画技巧
├── accessibility/              # 无障碍设计
└── platform-guidelines/        # 平台设计规范
```

---

## 📄 权威需求文档

**开发需求文档位置**: `D:\app-EvoMind\AI安卓App开发需求提示词：个人成长认知外骨骼App（EvoMind）.docx`

这是项目的权威需求文件，包含：
- 产品核心定位与开发原则
- 五大核心功能模块详细需求
- 技术架构与非功能需求
- 商业化与订阅收费体系
- UI/UX设计要求
- 开发交付物要求

每次开发前必须对照此文档确认需求理解正确。

---

## 🏗️ 项目架构核心概念

### 数据流架构

```
外部信息源 → 采集/导入 → Card(认知卡片) → Feed流(7:3混合)
                                                ↓
用户交互 → SocraticDialogue(苏格拉底对话) → Insight(洞察)
                                                ↓
                                       UserCorpus(用户语料库)
```

### 核心区别（必须牢记）

| 概念 | Card | UserCorpus |
|------|------|------------|
| 来源 | 外部信息源 | 用户自己生成 |
| Feed流 | ✅ 参与7:3推荐 | ❌ 不参与 |
| 用途 | 信息输入 | 知识沉淀 |
| 冲突检测 | ✅ 是 | ❌ 否 |

### 当前数据库实体关系

```
User
├── Card (认知卡片)
│   ├── MindMapNode (脑图节点)
│   ├── SourceContent (原文内容)
│   └── CardConflict (卡片间冲突)
├── UserCognitiveProfile (认知画像)
│   └── CognitiveConflict (认知冲突 - 卡片vs画像)
├── UserInterestProfile (兴趣画像)
├── UserReadingHistory (阅读历史)
├── SocraticDialogue (苏格拉底对话)
│   └── SocraticMessage (对话消息)
├── VoiceNote (语音笔记)
├── InfoSource (信息源)
│   └── ContentCrawlJob (采集任务)
├── CrawledContent (采集内容)
├── UserDailyFeedQuota (每日配额)
└── UserReadCardRecord (已读记录)
```

---

## 📊 功能完成状态（截至上次会话）

### ✅ 已完成（10个核心功能）

| ID | 功能 | 状态 |
|----|------|------|
| feat_001 | Android项目初始化 | ✅ |
| feat_002 | Spring Boot后端初始化 | ✅ |
| feat_003 | 用户注册与登录系统 | ✅ |
| feat_004 | 截图OCR识别功能 | ✅ |
| feat_005 | 链接抓取功能 | ✅ |
| feat_006 | 语音快速记录功能 | ✅ |
| feat_007 | AI认知卡片生成 | ✅ |
| feat_008 | 观点冲突标记系统 | ✅ |
| feat_009 | 7:3智能混合信息流 | ✅ |
| feat_010 | AI苏格拉底式对话 | ✅ |

### 🚧 待开发（高优先级）

| ID | 功能 | 依赖 | 说明 |
|----|------|------|------|
| feat_011 | 本地语料库管理 | UserCorpus实体 | 需要实现完整的语料库CRUD |
| feat_012 | 知识脑图自动生成 | MindMapNode已创建 | 需要可视化组件 |
| feat_013 | 一键训练私有Agent | ONNX Runtime | 本地AI训练 |
| feat_014 | Agent对话系统 | RAG检索 | 向量数据库 |

---

## 🔄 标准开发工作流程

### Step 1: 基线验证（10分钟）

```bash
# 1. 检查当前Git状态
cd EvoMind-Android
git status
git log --oneline -10

# 2. 检查数据库状态
cat backend/src/main/resources/db/migration/*.sql | tail -50

# 3. 启动后端服务验证
cd backend && mvn spring-boot:run
# 在另一个终端测试API
curl http://localhost:8080/api/v1/health
```

### Step 2: 功能开发（按以下顺序）

```markdown
1. **后端优先**（如果涉及新实体）
   - 创建/修改 Entity
   - 创建 Repository
   - 创建 Service 接口和实现
   - 创建 Controller API
   - 创建数据库迁移脚本
   - 编写单元测试

2. **Android数据层**
   - 创建 Domain 模型
   - 创建 API 接口 (Retrofit)
   - 创建 Repository
   - 创建 UseCase

3. **Android表现层**
   - 创建 ViewModel
   - 创建 Screen (Compose UI)
   - 集成导航
   - 添加权限/配置

4. **验证测试**
   - 运行后端单元测试
   - 手动测试API (curl/Postman)
   - 验证Android UI

5. **文档更新**
   - 更新 feature_list.json 标记完成
   - 更新 claude-progress.txt 记录变更
   - 更新相关架构文档

6. **Git提交**
   - git add .
   - git commit -m "feat: feat_XXX 功能描述"
```

---

## ⚠️ 常见错误防范

### 错误1: 需求理解偏差

**症状**: 开发的功能与预期不符，需要返工

**防范**:
- 严格遵循 Phase 4 功能理解确认
- 在编码前用文字描述功能逻辑
- 对照 app_spec.txt 验证理解

### 错误2: 重复造轮子

**症状**: 创建已存在的类似组件

**防范**:
- 开发前搜索现有代码: `search_files` 查找类似功能
- 检查已完成的 feat_XXX 是否有可复用代码
- 优先复用现有 Repository/Service 模式

### 错误3: 数据库不一致

**症状**: 代码与数据库结构不匹配

**防范**:
- 每次修改实体必须创建迁移脚本
- 使用 Flyway 版本控制
- 测试时清理并重建数据库

### 错误4: 忽略依赖关系

**症状**: 功能无法正常工作，缺少前置条件

**防范**:
- 严格遵循 Phase 3 依赖关系分析
- 在 feature_list.json 中标注功能依赖
- 优先开发被依赖的功能

---

## 📝 会话启动模板

每次新会话，复制以下内容作为第一条消息：

```markdown
## 会话启动检查清单

### 已完成上下文重建
- [ ] 读取 app_spec.txt
- [ ] 读取 feature_list.json（完成状态统计）
- [ ] 读取 claude-progress.txt
- [ ] 检查 docs/ 目录

### 已激活技能
- [ ] everything-claude-code: 移动应用开发技能
- [ ] everything-claude-code: API开发技能
- [ ] ui-ux-pro-max: Material Design 3 组件

### 待开发功能确认
- 功能ID: feat_XXX
- 功能名称: XXX
- 依赖状态: [依赖项是否完成]
- 理解摘要: [用1-2句话描述功能逻辑]

### 开发计划
1. [后端/前端] 步骤1
2. [后端/前端] 步骤2
3. ...

请确认以上理解是否正确，然后开始开发。
```

---

## 🎓 技能使用指南

### 何时使用 everything-claude-code

- 创建新的后端 API 时 → 使用 `api-development` 技能
- 设计数据库结构时 → 使用 `database-design` 技能
- 编写测试时 → 使用 `testing-strategies` 技能
- 安全相关功能 → 使用 `security-best-practices` 技能

### 何时使用 ui-ux-pro-max

- 设计新的 UI 页面时 → 使用 `design-systems` 技能
- 实现复杂交互时 → 使用 `component-patterns` 技能
- 添加动画效果时 → 使用 `animation-techniques` 技能
- 确保无障碍支持 → 使用 `accessibility` 技能

### 技能加载方式

```bash
# 在会话开始时加载技能
skill create-mcp-server --args="help"
skill create-mode --args="help"
```

---

## ✅ 会话结束检查清单

```markdown
- [ ] 功能已完整实现并通过测试
- [ ] feature_list.json 已更新（passes: true）
- [ ] claude-progress.txt 已记录本次变更
- [ ] Git 已提交（描述性提交信息）
- [ ] 代码已推送到远程仓库（如配置了）
- [ ] 环境已清理（无未提交变更）
```

---

## 📞 升级此协议

如果此协议有任何问题，请：
1. 在此文件中记录问题
2. 提出改进建议
3. 更新协议内容

---

**最后更新**: 2026-02-15
**版本**: v1.0
