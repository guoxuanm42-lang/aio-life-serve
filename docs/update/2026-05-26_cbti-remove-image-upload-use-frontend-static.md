# 2026-05-26_cbti-remove-image-upload-use-frontend-static

## 1. 更新内容

### 1.1 移除 CBTI 角色图片上传能力
- 管理端“CBTI 角色管理”列表移除“上传图”入口。
- 后端删除 `POST /cbti/admin/personalities/{code}/image` 上传接口。
- CBTI 相关接口返回不再拼接 `imageUrl=/file/preview/...`，不再依赖 MinIO 预览路由。

### 1.2 改为前端静态资源维护角色图片
- 角色图片改为由前端 `public` 静态目录提供：`/cbti-characters/{code}.png`。
- UI 侧增加加载失败降级：图片缺失时展示“无图/NO IMG”占位，避免页面空白或报错。

---

## 2. 修改原因

- 角色图片属于稳定素材，更适合以静态资源方式维护，避免上传与对象存储链路带来的运维成本。
- 降低 CBTI 模块对 MinIO 的耦合，使部署更轻量、问题定位更简单。

---

## 3. 迁移说明

### 3.1 图片放置位置

- 前端目录：`aio-life-front-main/aio-life-front-main/apps/web-antd/public/cbti-characters/`
- 文件命名：必须与人格 `code` 一致，且当前统一使用 `.png`
  - 示例：`996.png`、`BUG-0.png`、`AGILE.png`、`CTRL-C.png`、`JAVA.png`

### 3.2 从后端资源迁移图片到前端 public

当前项目已有一套 CBTI 角色图片资源位于后端：
`aio-life-serve-main/aio-life-serve-main/src/main/resources/cbti/characters/`

可用 PowerShell 执行复制（在仓库根目录）：

```powershell
Copy-Item -Force -Recurse `
  .\aio-life-serve-main\aio-life-serve-main\src\main\resources\cbti\characters\*.png `
  .\aio-life-front-main\aio-life-front-main\apps\web-antd\public\cbti-characters\
```

---

## 4. 技术实现细节

### 4.1 前端
- 文件：`aio-life-front-main/aio-life-front-main/apps/web-antd/src/views/_core/profile/cbti-setting.vue`
- 关键改动：
  - 删除“上传图”按钮与上传逻辑。
  - 所有图片展示统一改为 `GET /cbti-characters/{code}.png`。
  - 海报生成的图片绘制逻辑同步改为加载本地静态 URL。
- 文件：`aio-life-front-main/aio-life-front-main/apps/web-antd/src/api/core/cbti.ts`
  - 删除上传 API 定义，避免残留引用。

### 4.2 后端
- 文件：`aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/api/CbtiAdminController.java`
  - 删除上传接口与 MinIO 上传依赖。
  - 管理端列表 VO 不再派生 `imageUrl`。
- 文件：`aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/api/CbtiController.java`
  - `personalities/results/result` 等返回不再拼接 `imageUrl`。
- 文件：`aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/config/CbtiInitRunner.java`
  - 删除启动补传任务（不再向 MinIO 补传图片）。

---

## 5. 兼容性说明

- 数据库字段 `cbti_personality.image_object` 保留但停用（不再作为图片来源与 URL 拼接依据）。
- 若缺少对应图片文件，前端会显示占位提示，不影响功能流程。

