# EvoMind 自动化开发系统规则

## 开发流程 (10步循环)

每次开发新功能时，必须按以下步骤执行：

### Step 1: Get Bearings - 检查项目状态
- 执行 `git status` 检查当前Git状态
- 执行 `git log --oneline -5` 查看最近提交
- 确认工作区干净后再开始

### Step 2: 技能系统激活 - 检查可用技能
- 检查 .trae 目录下是否有相关技能配置
- 根据功能类型选择合适的技能

### Step 3: 验证基线 - 检查回归
- 检查 feature_list.json 中已通过功能
- 确认新功能不会破坏已有功能

### Step 4: 依赖分析 - 确认后端API存在
- 在 backend 目录下搜索相关Controller/Service
- 确认后端API已实现，再开发Android端
- 如果后端未实现，需要先创建后端基础设施

### Step 5: 功能理解 - 对照需求文档
- 在 docs 目录下查找需求文档
- 对照 04-android-uiux-and-modules.md 检查UI需求
- 对照 03-api-contract.md 检查API需求
- **需求文档主文件**: `D:\app-EvoMind\AI安卓App开发需求提示词：个人成长认知外骨骼App（EvoMind）.docx`

### Step 6: 增量开发 - 前后端实现
**后端开发顺序**:
1. Entity 实体类
2. Repository 数据层
3. Service 业务逻辑
4. Controller API控制器

**Android端开发顺序**:
1. DTO 数据传输对象
2. API Retrofit接口
3. Repository 仓库层
4. Domain Model 领域模型
5. ViewModel 状态管理
6. Screen UI页面

### Step 7: 验证测试 - 对照需求检查
- **必须打开需求文档**: `D:\app-EvoMind\AI安卓App开发需求提示词：个人成长认知外骨骼App（EvoMind）.docx`
- 对照需求文档检查功能完整性
- 检查UI组件是否齐全
- 检查API调用是否完整
- **必须列出需求文档中对应的功能描述**

### Step 8: 更新feature_list.json
- 将新功能标记为 passes: true
- 在steps中添加已完成的具体项

### Step 9: Git提交
- 执行 `git add -A`
- 执行 `git commit -m "feat: 功能描述"`
- 执行 `git push`

### Step 10: 更新进度日志
- 在 claude-progress.txt 中记录开发进度
- 包含时间戳、完成项、commit信息

---

## 开发优先级规则

1. **高优先级功能** - 优先开发
2. **中优先级功能** - 次优先开发
3. **低优先级功能** - 最后开发

## 已完成功能列表 (22个)
- feat_001-006: 基础架构与语音
- feat_007-010: 冲突检测
- feat_011: 语料库管理
- feat_012: 知识脑图
- feat_014: Agent对话系统
- feat_015: 挑战任务系统
- feat_021: 算力成本系统
- feat_073-079: 用户系统与合规

## 待开发功能 (68个)
按优先级分组开发
