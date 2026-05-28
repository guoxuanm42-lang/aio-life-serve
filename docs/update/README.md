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

## 模块说明

| 模块 | 说明 |
|---|---|
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
