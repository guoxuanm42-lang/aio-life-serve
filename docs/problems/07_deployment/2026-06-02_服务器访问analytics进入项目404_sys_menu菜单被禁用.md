# 2026-06-02_服务器访问analytics进入项目404_sys_menu菜单被禁用

## 1. 现象

- 项目部署到服务器后，访问 `http://150.158.105.184:8080/#/analytics`。
- 页面进入 AIO-LIFE 项目自带的 404 页面，提示“哎呀！未找到页面”。
- 该 404 不是 Nginx 404，也不是前端容器未启动，而是前端应用内部路由没有匹配到 `/analytics` 页面。

---

## 2. 根因

AIO-LIFE 当前使用后端菜单模式。前端启动后会通过接口读取后端菜单数据，再动态注册前端路由：

```http
GET /api/menu/all
```

服务器数据库 `sys_menu` 表中 `/analytics` 菜单记录被禁用：

```text
path = /analytics
status = 0
```

因此 `/api/menu/all` 不返回 `/analytics` 这条菜单，前端不会动态注入该路由。直接访问 `/#/analytics` 时，路由无法匹配，最终进入项目自身的 404 兜底页。

链路如下：

```text
sys_menu 中 /analytics 被禁用
  ↓
/api/menu/all 不返回 /analytics
  ↓
前端没有注册 /analytics 路由
  ↓
访问 /#/analytics 进入项目 404
```

---

## 3. 可能触发原因

### 3.1 菜单被改成禁用状态

如果下午可以访问、晚上突然 404，说明 `/analytics` 曾经存在于 `/api/menu/all` 返回结果中，后续又从返回结果中消失。

常见来源：

- 在“系统管理 / 权限菜单”页面手动关闭了该菜单。
- 直接执行 SQL 修改了 `sys_menu`。
- 执行初始化脚本或同步菜单脚本，覆盖了菜单状态。
- 重置菜单数据时把 `/analytics` 写成了禁用状态。

### 3.2 登录态或权限变化

如果登录态过期、重新登录或切换账号，前端会重新调用 `/api/menu/all`，不同账号可能拿到不同菜单。

本次已确认数据库中 `/analytics` 的 `status=0`，所以登录态不是主因，只作为辅助排查方向。

### 3.3 后端重启、数据库切换或初始化回退

如果晚上重启过后端、重新部署过服务，或执行过初始化 SQL，可能导致菜单数据被覆盖或后端连接到另一套数据库。

---

## 4. 解决方案

重新启用 `/analytics` 菜单：

```sql
UPDATE sys_menu
SET status = 1
WHERE path = '/analytics';
```

执行后刷新页面。如果仍然进入 404，退出登录后重新登录，让前端重新调用 `/api/menu/all` 获取最新菜单并重新注入动态路由。

---

## 5. 验证方式

### 5.1 检查接口返回

重新登录后打开浏览器开发者工具：

```text
F12 -> Network
```

查看接口：

```http
GET /api/menu/all
```

确认响应中包含：

```json
"path": "/analytics"
```

### 5.2 直接访问页面

重新访问：

```text
http://150.158.105.184:8080/#/analytics
```

如果页面正常打开，说明 `/analytics` 路由已成功动态注入。

---

## 6. 追踪是谁修改了菜单

如果 `sys_menu` 表中有更新时间、更新人字段，可以查询：

```sql
SELECT id, name, path, status, updated_at, updated_by
FROM sys_menu
WHERE path = '/analytics';
```

如果字段名不是 `updated_at`、`updated_by`，先查看表结构：

```sql
DESC sys_menu;
```

再根据实际字段名调整查询语句。

---

## 7. 结论

本次 `/#/analytics` 进入 404 的根因不是服务器异常，也不是 Nginx、Docker 或前端容器问题，而是：

```text
后端菜单表 sys_menu 中 /analytics 被禁用，导致前端动态路由中没有注册该页面。
```

恢复方式：

```sql
UPDATE sys_menu
SET status = 1
WHERE path = '/analytics';
```

然后刷新页面或重新登录，让前端重新拉取菜单即可恢复访问。

---

## 8. 相关排查：菜单排序不生效

如果线上出现“权限菜单”中已经修改 `sort`，但左侧侧边栏顺序没有变化，需要区分数据库排序字段和前端运行时排序字段。

当前约定：

```text
侧边栏排序 = /api/menu/all 返回的 meta.order = sys_menu.sort
```

后台“权限菜单”的 `sort` 字段是唯一需要人工维护的排序入口，`meta.order` 不再需要手动维护。

排查步骤：

1. 打开浏览器开发者工具，查看：

```http
GET /api/menu/all
```

2. 找到目标菜单，确认响应中的：

```json
"meta": {
  "order": 目标排序值
}
```

3. 对比数据库：

```sql
SELECT id, name, path, sort, meta
FROM sys_menu
WHERE path = '/my-hub/food-record';
```

如果 `/api/menu/all` 返回的 `meta.order` 不等于 `sys_menu.sort`，说明后端仍是旧版本，优先重新部署后端排序同步逻辑。

不要优先修改前端排序逻辑；前端继续按 `meta.order` 排序，后端负责把 `sys_menu.sort` 映射为运行时 `meta.order`。
