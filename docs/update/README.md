# 人生系统更新记录

本目录用于记录 aio-life 人生系统的每次功能更新、问题修复、UI 优化和部署变更。

## 目录规则

- 按年份和月份归档（`YYYY/MM`）
  - 本目录当前为历史平铺结构，暂未迁移；后续新增文档建议按 `YYYY/MM` 归档
- 每次较完整的更新写一篇 Markdown（或按功能点拆分）
- 文件命名格式：`日期_模块_主题_类型.md`
  - 日期：`YYYY-MM-DD`
  - 模块：使用下方“模块说明”中的 key
  - 主题：建议使用 `kebab-case`（短横线）描述（避免中文/空格导致兼容问题）
  - 类型：建议使用以下枚举
    - `update`：功能新增 / UI 优化 / 行为变更
    - `fix`：问题修复
    - `deploy`：部署 / 构建 / Nginx / Docker 相关
- 历史文件说明
  - 现有部分历史文件未完全遵循以上命名规范，暂不做迁移与重命名（如需统一，可单独开迁移任务）

## 文档标题规则

- 每篇更新文档必须使用一个一级标题，格式为：`# YYYY-MM-DD 中文更新主题`。
- 一级标题必须包含完整日期，日期格式固定为 `YYYY-MM-DD`。
- 一级标题的主题必须使用简洁、明确的中文描述，不使用纯英文标题。
- 禁止使用 `git commit -m "..."`、文件名或命令作为一级标题。
- 如需记录 Git 提交信息，应放在“提交建议”等独立二级章节中，并使用代码块展示。
- 文档日期应与文件名日期保持一致；更新主题应与文件名中的主题语义一致。

## 内容维护规则

每篇更新文档建议包含以下内容：

- 背景：说明为什么要做这次更新。
- 解决的问题：说明本次更新实际解决了哪些痛点、缺陷或体验问题。
- 功能使用说明：说明用户如何使用新增或变更后的功能。
- 本次调整：说明数据库、后端、前端、接口、统计等实际变更。
- 验证范围：记录已执行的编译、接口、数据库或前端验证。
- 后续建议：记录暂未完成但值得继续优化的事项。

如果后续更新涉及功能入口、页面交互、字段含义、状态流转、统计口径或用户操作方式变化，必须同步更新该文档中的“功能使用说明”内容，避免只记录技术实现而缺少使用说明。

## 模块说明

| 模块 | 说明 |
|---|---|
| ai | AI 对话、Agent、记忆与活动总结 |
| thought | 闪念模块 |
| cbti | CBTI / 人格测试 |
| auth | 登录、注册、权限 |
| storage | 文件、图片、MinIO |
| deployment | 部署、Docker、Nginx |
| server | 服务器环境 |
| common | 通用组件、通用样式 |

## 最近更新

| 日期 | 模块 | 更新主题 | 类型 | 文档 |
|---|---|---|---|---|
| 2026-08-15 | ai | AI 活动总结生成、消息持久化与前端交互 | update | [2026-08-15_ai-activity-summary-generation_update.md](./2026/08/2026-08-15_ai-activity-summary-generation_update.md) |
| 2026-07-20 | common | AI 流式通信安全与稳定性 | update | [2026-07-20_common-ai-stream-security-and-stability_update.md](./2026/07/2026-07-20_common-ai-stream-security-and-stability_update.md) |
| 2026-07-14 | todo | 失败复盘页增加代办删除操作 | update | [2026-07-14_todo-review-delete-action_update.md](./2026-07-14_todo-review-delete-action_update.md) |
| 2026-07-14 | auth | API Key 收缩为 MCP 协议专用凭证 | update | [2026-07-14_auth-api-key-mcp-only-access_update.md](./2026-07-14_auth-api-key-mcp-only-access_update.md) |
| 2026-07-14 | auth | API Key 认证日志脱敏 | fix | [2026-07-14_auth-api-key-log-masking_fix.md](./2026-07-14_auth-api-key-log-masking_fix.md) |
| 2026-06-12 | thought | 闪念多类型、结构化详情与状态流转日志 | update | [2026-06-12_thought-type-detail-status-log_update.md](./2026-06-12_thought-type-detail-status-log_update.md) |
| 2026-06-10 | thought | 闪念统计洞察与时间趋势 | update | [2026-06-10_thought-statistics-insight_update.md](./2026-06-10_thought-statistics-insight_update.md) |
| 2026-06-05 | common | 美食记录 Agent MCP 文档 | update | [2026-06-05_common-food-record-agent-mcp-docs_update.md](./2026-06-05_common-food-record-agent-mcp-docs_update.md) |
| 2026-06-05 | thought | 闪念主题内容模糊搜索 | update | [2026-06-05_thought-subject-fuzzy-search_update.md](./2026-06-05_thought-subject-fuzzy-search_update.md) |
| 2026-06-04 | common | MCP 工具管理、参考中心与模拟调用 | update | [2026-06-04_common-mcp-tool-management-reference-center_update.md](./2026-06-04_common-mcp-tool-management-reference-center_update.md) |
| 2026-06-02 | server | 后端启动端口 45678 占用处理 | update | [2026-06-02_backend-port-45678-conflict-fix_update.md](./2026-06-02_backend-port-45678-conflict-fix_update.md) |
| 2026-06-02 | auth | 菜单排序字段同步为前端 meta.order | update | [2026-06-02_menu-sort-sync-meta-order_update.md](./2026-06-02_menu-sort-sync-meta-order_update.md) |
| 2026-05-31 | common | MCP 工具重构、调用链标准化与闪念导入 | update | [2026-05-31_common-mcp-tool-refactor-and-thought-import_update.md](./2026-05-31_common-mcp-tool-refactor-and-thought-import_update.md) |
| 2026-05-31 | thought | 闪念详情局部编辑与已搁置状态 | update | [2026-05-31_thought-detail-inline-edit-and-shelved-status_update.md](./2026-05-31_thought-detail-inline-edit-and-shelved-status_update.md) |
| 2026-05-31 | todo | 待办清单、复盘配置与 SQL 规范 | update | [2026-05-31_todo-list-review-config-and-sql-standard_update.md](./2026-05-31_todo-list-review-config-and-sql-standard_update.md) |
| 2026-05-28 | common | MCP/REST 分离与工具契约稳定化 | update | [2026-05-28_common_mcp-rest-controller-split-and-tool-stabilization_update.md](./2026-05-28_common_mcp-rest-controller-split-and-tool-stabilization_update.md) |
| 2026-05-26 | cbti | 移除上传图片，改为前端静态角色图片 | update | [2026-05-26_cbti-remove-image-upload-use-frontend-static.md](./2026-05-26_cbti-remove-image-upload-use-frontend-static.md) |
| 2026-05-26 | thought | 闪念状态字段与筛选 | update | [2026-05-26_thought-status-field-and-filter.md](./2026-05-26_thought-status-field-and-filter.md) |
| 2026-05-26 | thought | 闪念卡片 UI（徽章/玻璃边框/排版） | update | [2026-05-26_thought-card-ui-badge-glass-border-typography.md](./2026-05-26_thought-card-ui-badge-glass-border-typography.md) |
| 2026-05-26 | thought | 闪念分类颜色映射修复 | fix | [2026-05-26_thought-category-color-mapping-fix.md](./2026-05-26_thought-category-color-mapping-fix.md) |
| 2026-05-25 | storage | MinIO 对象命名结构（方案 A） | update | [2026-05-25_minio-object-naming-scheme-a.md](./2026-05-25_minio-object-naming-scheme-a.md) |
| 2026-05-23 | thought | 闪念标题与主题色 | update | [2026-05-23_thought-subject-and-theme-color.md](./2026-05-23_thought-subject-and-theme-color.md) |
| 2026-04-30 | auth | 菜单后端模式与密码依赖修复 | fix | [2026-04-30_menu-backend-mode-and-password-dependency-fix.md](./2026-04-30_menu-backend-mode-and-password-dependency-fix.md) |
| 2026-04-22 | cbti | CBTI 角色管理后台优化 | update | [2026-04-22_cbti-role-admin-ui-optimization.md](./2026-04-22_cbti-role-admin-ui-optimization.md) |
| 2026-04-20 | auth | 受保护菜单禁用 guard | fix | [2026-04-20_protected-menu-disable-guard.md](./2026-04-20_protected-menu-disable-guard.md) |
