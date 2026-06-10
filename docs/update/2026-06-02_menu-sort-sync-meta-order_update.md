# 菜单排序字段与前端侧边栏顺序约定更新（2026-06-02）

## 1. 背景

当前项目使用后端菜单模式，前端通过：

```http
GET /api/menu/all
```

获取后端返回的路由树，再动态注册路由并生成侧边栏菜单。

前端侧边栏最终排序使用的是菜单 `meta.order`，而管理端“权限菜单”页面维护的是数据库字段 `sys_menu.sort`。如果两者不一致，就容易出现后台排序字段已修改，但侧边栏顺序看起来没有变化的问题。

---

## 2. 本次约定

从本次调整开始：

- 后台“权限菜单”的 `sort` 字段是侧边栏排序的唯一人工维护入口。
- `meta.order` 不再需要人工维护。
- `/api/menu/all` 会在运行时自动把 `sys_menu.sort` 映射为返回结构里的 `meta.order`。
- 即使历史 `sys_menu.meta` 中已经存在 `order`，接口返回时也会以 `sys_menu.sort` 覆盖。
- `/api/menu/admin/tree` 仍返回数据库原始 `meta`，不会注入运行时 `order`，避免编辑弹窗中出现派生字段。

核心规则：

```text
侧边栏排序 = /api/menu/all 返回的 meta.order = sys_menu.sort
```

---

## 3. 数据处理策略

本阶段不清理历史数据，也不回改历史 SQL。

原因：

- 历史 SQL 中已有的 `meta.order` 属于旧版本种子配置，保留不影响运行时结果。
- 线上 `sys_menu.meta.order` 即使存在，也会被 `/api/menu/all` 运行时注入的 `sys_menu.sort` 覆盖。
- 不新增清理 SQL，可以避免误删 `meta` 中其他有效字段。

后续维护菜单顺序时，只需要修改：

```text
sys_menu.sort
```

不需要再同步修改：

```text
sys_menu.meta.order
```

---

## 4. 验证方式

### 4.1 接口验证

请求：

```http
GET /api/menu/all
```

检查任一菜单节点：

```json
{
  "path": "/my-hub/food-record",
  "meta": {
    "title": "美食",
    "order": 5
  }
}
```

确认 `meta.order` 等于数据库中该菜单的 `sys_menu.sort`。

### 4.2 管理端验证

请求：

```http
GET /api/menu/admin/tree
```

确认管理端返回的是数据库原始 `meta`，不会因为运行时排序映射而额外写入 `order`。

### 4.3 侧边栏验证

- 修改一级菜单排序后保存，确认侧边栏立即刷新并按新顺序显示。
- 修改同一父菜单下的二级菜单排序，确认子菜单顺序变化。
- 退出登录后重新登录，确认顺序仍一致。

建议覆盖菜单：

- 一级菜单：主页、仪表盘、待办、美食、系统管理。
- 二级菜单：待办清单、复盘、配置。

---

## 5. 排查建议

如果线上侧边栏排序仍不生效，优先检查后端是否已经部署本次排序同步逻辑。

判断方式：

1. 查看 `/api/menu/all` 返回的 `meta.order`。
2. 对比数据库 `sys_menu.sort`。
3. 如果两者不一致，说明后端仍是旧版本。

不要优先修改前端排序逻辑。当前前端继续使用 `meta.order` 排序，后端负责把 `sys_menu.sort` 转成运行时 `meta.order`。
