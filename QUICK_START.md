# EvoMind 快速启动指南

## 🚀 每次新对话的操作步骤

### 步骤1: 发送启动指令

在新对话中，复制并发送以下内容：

```markdown
请严格按照 SESSION_START_PROTOCOL.md 执行会话启动检查清单，然后告诉我：
1. 已完成的功能数量
2. 最高优先级的待开发功能
3. 该功能的依赖状态
```

### 步骤2: 确认理解

我会读取以下文件来重建上下文：
- `app_spec.txt` - 项目规格
- `feature_list.json` - 功能清单
- `claude-progress.txt` - 开发日志
- `docs/` - 架构文档

### 步骤3: 确认功能理解

我会用文字描述对该功能的理解，你需要确认：
- 功能逻辑是否正确
- 边界情况是否考虑
- 与已有功能的关系

### 步骤4: 开始开发

确认无误后，我会按照以下顺序开发：
1. 后端（Entity → Repository → Service → Controller → Migration）
2. Android 数据层（Domain → Api → Repository → UseCase）
3. Android 表现层（ViewModel → Screen）
4. 测试和验证
5. 更新文档并提交 Git

---

## 📊 当前项目状态（实时）

### 已完成功能（10个）

| ID | 功能 | 说明 |
|----|------|------|
| feat_001 | Android项目初始化 | Kotlin + Jetpack Compose + MVVM |
| feat_002 | Spring Boot后端初始化 | Java 17 + Spring Boot 3.x |
| feat_003 | 用户注册与登录系统 | 手机号/微信登录 |
| feat_004 | 截图OCR识别功能 | 百度OCR SDK集成 |
| feat_005 | 链接抓取功能 | 小红书/微信/知乎/微博 |
| feat_006 | 语音快速记录功能 | 百度语音API |
| feat_007 | AI认知卡片生成 | DeepSeek API集成 |
| feat_008 | 观点冲突标记系统 | 新卡片vs用户认知画像 |
| feat_009 | 7:3智能混合信息流 | 推荐算法与信息茧房避免 |
| feat_010 | AI苏格拉底式对话 | 5轮渐进式追问 |

### 待开发功能（高优先级）

| ID | 功能 | 依赖 | 状态 |
|----|------|------|------|
| feat_011 | 本地语料库管理 | UserCorpus实体 | 🚧 规划中 |
| feat_012 | 知识脑图自动生成 | MindMapNode已创建 | 🚧 规划中 |
| feat_013 | 一键训练私有Agent | ONNX Runtime | ⏳ 待开发 |
| feat_014 | Agent对话系统 | RAG检索 | ⏳ 待开发 |

---

## 🎯 下一步开发建议

根据 claude-progress.txt，下一步建议开发：

### 选项1: feat_011 本地语料库管理
- **依赖**: 需要创建 UserCorpus 实体
- **说明**: 实现用户语料库的 CRUD、搜索、收藏、置顶、归档
- **参考文档**: `docs/09-user-corpus-architecture.md`

### 选项2: feat_012 知识脑图自动生成
- **依赖**: MindMapNode 实体已创建
- **说明**: 完善脑图的可视化展示和手动编辑功能
- **已有代码**: `MindMapService`, `MindMapNode` 实体

---

## 📁 重要文件位置

```
EvoMind-Android/
├── app_spec.txt                    # 项目规格（需求源头）
├── feature_list.json               # 功能清单（完成情况）
├── claude-progress.txt             # 开发日志（历史记录）
├── SESSION_START_PROTOCOL.md       # 会话启动协议（必看）
├── SKILL_INDEX.md                  # 技能资源索引（必看）
├── SESSION_TEMPLATE.md             # 会话启动模板（复制使用）
├── QUICK_START.md                  # 本文件
├── docs/
│   └── 09-user-corpus-architecture.md  # 语料库架构
├── backend/                        # Spring Boot 后端
│   └── src/main/java/com/evomind/
└── android-app/                    # Android 前端
    └── app/src/main/java/com/evomind/
```

---

## 💡 提示

1. **每次新对话都要发送启动指令**，确保我正确重建上下文
2. **如有疑问请先确认**，不要假设我理解正确
3. **严格遵循开发顺序**，先做后端再做前端
4. **及时更新文档**，保持 feature_list.json 和 claude-progress.txt 同步

---

**最后更新**: 2026-02-15
