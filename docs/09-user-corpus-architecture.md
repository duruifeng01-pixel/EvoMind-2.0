# 用户语料库架构设计

## 1. 架构概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              EvoMind 内容架构                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        Feed 流 (Card)                                │   │
│  │  ┌─────────────────┐    ┌─────────────────┐                         │   │
│  │  │  70% 用户自选源  │ +  │  30% 系统推荐   │  ← 外部认知输入          │   │
│  │  │ (CrawledContent)│    │ (Discovery)     │                         │   │
│  │  └─────────────────┘    └─────────────────┘                         │   │
│  │                                                                     │   │
│  │  特点：                                                              │   │
│  │  - 来自外部信息源（公众号、知乎、RSS等）                                │   │
│  │  - 经过AI处理生成认知卡片                                               │   │
│  │  - 参与 7:3 智能混合推荐流                                              │   │
│  │  - 支持冲突检测、脑图生成                                               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   ↑                                         │
│                                   │ 分离                                     │
│                                   ↓                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     用户语料库 (UserCorpus)                           │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │   │
│  │  │ 苏格拉底洞察  │  │  用户笔记     │  │  收藏高亮    │               │   │
│  │  │  (Insight)   │  │   (Note)     │  │ (Highlight)  │               │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘               │   │
│  │                                                                     │   │
│  │  特点：                                                              │   │
│  │  - 用户自己生成的知识资产                                               │   │
│  │  - 不参与 Feed 流推荐（避免信息茧房）                                    │   │
│  │  - 支持搜索、收藏、置顶、归档                                            │   │
│  │  - 是用户的个人知识库                                                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 2. 核心区别

| 维度 | Card (认知卡片) | UserCorpus (用户语料库) |
|------|----------------|------------------------|
| **来源** | 外部信息源 | 用户自己生成 |
| **内容类型** | 采集的文章、资讯 | 洞察、笔记、高亮 |
| **Feed流参与** | ✅ 是（7:3混合推荐） | ❌ 否（完全分离） |
| **茧房风险** | 需要防范 | 无（用户主动产出） |
| **搜索支持** | ✅ 是 | ✅ 是 |
| **冲突检测** | ✅ 是（认知冲突） | ❌ 否 |
| **脑图生成** | ✅ 是 | ❌ 否 |
| **主要目的** | 信息输入、打破茧房 | 知识沉淀、个人资产 |

## 3. UserCorpus 实体设计

### 3.1 语料类型 (CorpusType)

```java
public enum CorpusType {
    SOCRATIC_INSIGHT,  // 苏格拉底式对话洞察
    USER_NOTE,         // 用户笔记/随想
    HIGHLIGHT,         // 收藏高亮/标注
    AI_SUMMARY,        // AI辅助总结
    REFLECTION,        // 深度反思
    INSIGHT            // 其他洞察
}
```

### 3.2 来源类型 (SourceType)

```java
public enum SourceType {
    SOCRATIC_DIALOGUE,  // 苏格拉底式对话
    DISCUSSION,         // 讨论
    CARD,               // 原始卡片
    MANUAL_INPUT,       // 手动输入
    VOICE_NOTE,         // 语音笔记
    OCR_IMPORT,         // OCR导入
    LINK_SCRAPE         // 链接采集
}
```

### 3.3 核心字段

| 字段 | 说明 | 用途 |
|------|------|------|
| `user_id` | 用户ID | 归属标识 |
| `title` | 标题 | 展示用 |
| `content_text` | 完整内容 | 存储洞察/笔记原文 |
| `corpus_type` | 语料类型 | 分类筛选 |
| `source_type` | 来源类型 | 追溯来源 |
| `source_id` | 来源ID | 关联查询 |
| `discussion_id` | 讨论ID | 关联讨论 |
| `is_pinned` | 是否置顶 | 优先展示 |
| `is_archived` | 是否归档 | 归档管理 |
| `is_favorite` | 是否收藏 | 收藏夹 |

## 4. 苏格拉底式对话集成

### 4.1 洞察保存流程

```
用户完成苏格拉底式对话
         ↓
AI 生成最终洞察 (finalInsight)
         ↓
构建对话过程摘要 (dialogueSummary)
         ↓
调用 UserCorpusService.createFromSocraticDialogue()
         ↓
创建 UserCorpus 记录
         ↓
返回 corpusId（而非 cardId）
```

### 4.2 代码变更

**SocraticDialogueServiceImpl.java:**

```java
// 修改前：保存为 Card
Card savedCard = cardService.createCard(...);
return savedCard.getId();

// 修改后：保存到 UserCorpus
UserCorpus corpus = userCorpusService.createFromSocraticDialogue(
    userId, dialogueId, discussionId, title, insight, dialogueSummary
);
return corpus.getId();
```

## 5. API 接口

### 5.1 语料库管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/corpus` | GET | 获取语料列表 |
| `/api/v1/corpus/{id}` | GET | 获取语料详情 |
| `/api/v1/corpus/archived` | GET | 获取归档语料 |
| `/api/v1/corpus/favorites` | GET | 获取收藏语料 |
| `/api/v1/corpus/type/{type}` | GET | 按类型筛选 |
| `/api/v1/corpus/pinned` | GET | 获取置顶语料 |
| `/api/v1/corpus/search` | GET | 搜索语料 |
| `/api/v1/corpus/{id}/favorite` | POST | 切换收藏 |
| `/api/v1/corpus/{id}/pin` | POST | 切换置顶 |
| `/api/v1/corpus/{id}/archive` | POST | 归档语料 |
| `/api/v1/corpus/{id}/unarchive` | POST | 取消归档 |
| `/api/v1/corpus/{id}` | DELETE | 删除语料 |
| `/api/v1/corpus/stats` | GET | 获取统计 |
| `/api/v1/corpus/discussion/{id}` | GET | 获取讨论相关洞察 |

### 5.2 苏格拉底对话接口更新

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/socratic/dialogues/{id}/save-insight` | POST | 保存洞察（返回 corpusId） |

## 6. 数据库表结构

```sql
CREATE TABLE user_corpus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content_text LONGTEXT,
    summary_text VARCHAR(1000),
    one_sentence_summary VARCHAR(200),
    corpus_type VARCHAR(30) NOT NULL DEFAULT 'SOCRATIC_INSIGHT',
    source_type VARCHAR(30),
    source_id BIGINT,
    source_ref VARCHAR(512),
    discussion_id BIGINT,
    keywords VARCHAR(500),
    reading_time_minutes INT,
    is_favorite TINYINT(1) DEFAULT 0,
    is_pinned TINYINT(1) DEFAULT 0,
    pinned_at DATETIME,
    is_archived TINYINT(1) DEFAULT 0,
    archived_at DATETIME,
    view_count INT DEFAULT 0,
    last_viewed_at DATETIME,
    related_card_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_user_archived (user_id, is_archived),
    INDEX idx_corpus_type (corpus_type),
    INDEX idx_source (source_type, source_id),
    INDEX idx_discussion (discussion_id)
);
```

## 7. 设计原则

### 7.1 为什么分离？

1. **避免信息茧房**: 用户自己的洞察如果进入 Feed 流，会强化已有认知
2. **明确内容边界**: 外部输入 vs 内部产出，概念清晰
3. **不同管理需求**: Feed 流内容消费后即走，语料库内容需要长期管理
4. **搜索场景不同**: Feed 流是"发现"，语料库是"回顾"

### 7.2 未来扩展

- **笔记编辑器**: 支持用户手动创建笔记
- **高亮导入**: 从外部阅读器导入高亮标注
- **语音转文字**: 语音笔记自动转为语料
- **AI 辅助总结**: 对长内容生成AI摘要保存
- **知识图谱**: 语料库内容参与个人知识图谱构建

## 8. 总结

通过 `UserCorpus` 的引入，我们实现了：

1. ✅ **清晰的架构边界**: Feed 流 vs 语料库，外部输入 vs 内部产出
2. ✅ **苏格拉底洞察归位**: 对话洞察进入语料库，成为用户知识资产
3. ✅ **避免茧房风险**: 用户洞察不参与 Feed 流推荐
4. ✅ **完善的语料管理**: 支持收藏、置顶、归档、搜索等功能
5. ✅ **可扩展的设计**: 未来可支持笔记、高亮等多种类型
