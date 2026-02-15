# EvoMind 技能索引

> 项目开发技能资源索引，指导何时使用何种技能

---

## 📚 可用技能资源

### 1. everything-claude-code-main 技能库

位置: `../everything-claude-code-main/skills/`

| 技能名称 | 适用场景 | 优先级 |
|---------|---------|--------|
| [`springboot-patterns`](everything-claude-code-main/skills/springboot-patterns/SKILL.md) | Spring Boot 后端开发模式 | ⭐⭐⭐⭐⭐ |
| [`springboot-tdd`](everything-claude-code-main/skills/springboot-tdd/SKILL.md) | Spring Boot 测试驱动开发 | ⭐⭐⭐⭐ |
| [`springboot-security`](everything-claude-code-main/skills/springboot-security/SKILL.md) | Spring Security 安全配置 | ⭐⭐⭐⭐ |
| [`springboot-verification`](everything-claude-code-main/skills/springboot-verification/SKILL.md) | Spring Boot 验证与校验 | ⭐⭐⭐ |
| [`jpa-patterns`](everything-claude-code-main/skills/jpa-patterns/SKILL.md) | JPA 数据库设计模式 | ⭐⭐⭐⭐⭐ |
| [`java-coding-standards`](everything-claude-code-main/skills/java-coding-standards/SKILL.md) | Java 编码规范 | ⭐⭐⭐⭐ |
| [`database-migrations`](everything-claude-code-main/skills/database-migrations/SKILL.md) | 数据库迁移脚本 | ⭐⭐⭐⭐ |
| [`api-design`](everything-claude-code-main/skills/api-design/SKILL.md) | REST API 设计 | ⭐⭐⭐⭐ |
| [`security-review`](everything-claude-code-main/skills/security-review/SKILL.md) | 安全审查 | ⭐⭐⭐ |
| [`tdd-workflow`](everything-claude-code-main/skills/tdd-workflow/SKILL.md) | TDD 开发流程 | ⭐⭐⭐ |

### 2. ui-ux-pro-max-skill-main 技能库

位置: `../ui-ux-pro-max-skill-main/`

| 技能名称 | 适用场景 | 优先级 |
|---------|---------|--------|
| [`design_systems`](ui-ux-pro-max-skill-main/src/ui-ux-pro-max/templates/base/skill-content.md) | 设计系统与组件库 | ⭐⭐⭐⭐ |
| Material Design 3 组件 | Android UI 组件 | ⭐⭐⭐⭐⭐ |

---

## 🎯 按功能模块推荐技能

### 用户认证模块
- **技能**: `springboot-security`, `springboot-patterns`
- **场景**: 密码登录、忘记密码、JWT认证

### 支付系统模块
- **技能**: `springboot-patterns`, `security-review`
- **场景**: 微信支付、支付宝支付、订单管理

### 数据库设计
- **技能**: `jpa-patterns`, `database-migrations`
- **场景**: 实体设计、Repository模式、迁移脚本

### AI功能开发
- **技能**: `springboot-patterns`, `api-design`
- **场景**: DeepSeek API集成、多模型路由

### 内容合规系统
- **技能**: `security-review`, `springboot-verification`
- **场景**: AIGC审核、敏感词过滤

### Android UI开发
- **技能**: `design_systems` (ui-ux-pro-max)
- **场景**: 页面设计、主题切换、组件库

---

## 📝 技能使用示例

### 使用 springboot-patterns 技能

```bash
# 当需要创建新的 Service/Controller 时
skill springboot-patterns --args="layered-architecture"
```

### 使用 jpa-patterns 技能

```bash
# 当需要设计数据库实体关系时
skill jpa-patterns --args="entity-design"
```

### 使用 springboot-security 技能

```bash
# 当需要配置安全认证时
skill springboot-security --args="jwt-setup"
```

---

## 🔧 技能激活检查清单

每次开发前确认：

- [ ] 已检查 everything-claude-code-main/skills/ 相关技能
- [ ] 已阅读技能文档了解使用方式
- [ ] 已根据当前任务选择并加载相关技能

---

## 📊 技能使用统计

| 技能 | 使用次数 | 上次使用 |
|------|---------|---------|
| springboot-patterns | 12 | 2026-02-15 |
| jpa-patterns | 8 | 2026-02-15 |
| springboot-tdd | 4 | 2026-02-14 |
| api-design | 6 | 2026-02-15 |

---

**最后更新**: 2026-02-15
