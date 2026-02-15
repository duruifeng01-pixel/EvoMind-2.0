# 新会话启动模板

复制以下内容，在新会话开始时粘贴：

---

## 🚀 EvoMind 开发会话启动

请严格遵循 [SESSION_START_PROTOCOL.md](./SESSION_START_PROTOCOL.md) 执行以下检查清单：

### Phase 1: 项目上下文重建

请读取以下文件重建项目上下文：
1. `app_spec.txt` - 项目整体规格
2. `feature_list.json` - 功能清单和完成状态  
3. `claude-progress.txt` - 开发历史和当前进度
4. `docs/` 目录下的架构文档

### Phase 2: 技能系统激活

项目根目录下有以下技能资源可用：
- `../everything-claude-code-main/skills/` - Claude Code 技能
- `../ui-ux-pro-max-skill-main/src/` - UI/UX Pro Max 技能

请列出可用技能并选择当前任务相关的技能。

### Phase 3: 功能开发确认

根据 feature_list.json，当前最高优先级的待开发功能是：
**feat_XXX: [功能名称]**

请确认：
1. 该功能的所有依赖项是否已完成？
2. 该功能的数据库实体是否已创建？
3. 该功能与已有功能的关系是什么？

### Phase 4: 开始开发

在确认以上信息后，按照以下顺序开发：
1. 后端实体/Repository/Service/Controller
2. 数据库迁移脚本
3. Android 数据层 (Domain/Api/Repository/UseCase)
4. Android 表现层 (ViewModel/Screen)
5. 测试和验证
6. 更新文档和 Git 提交

---

当前要开发的功能是：**[请用户填写]**
