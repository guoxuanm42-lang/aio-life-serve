# 相册功能一期至三期更新

## 背景

本次更新为 `aio-life` 增加个人相册能力，目标是支持用户按文件夹管理旅游、生活等照片，并能在电脑端和手机端浏览、放大、预览和管理图片。

功能分三阶段落地：

- 阶段一：最小可用相册，先实现上传、分类、浏览、放大。
- 阶段二：图片管理增强，补齐标题备注、移动、删除、封面、搜索、数量统计和面包屑。
- 阶段三：浏览体验优化，让页面更接近手机系统相册和桌面图片查看器。

## 解决的问题

- 解决个人照片缺少统一存储和分类管理入口的问题。
- 解决图片只能存储但不能按多级文件夹组织的问题。
- 解决图片预览不够沉浸、手机端浏览不方便的问题。
- 解决大图列表一次性加载 Blob 带来的性能浪费问题。
- 解决照片文件超过 Spring Boot 默认 `1MB` 上传限制导致上传失败的问题。
- 解决相册 SQL 文件编码损坏、无法直接执行的问题。

## 本次功能范围

### 后端相册模块

新增统一接口前缀：

- `/photo-album/**`

新增 Controller：

- `PhotoAlbumController`

新增 Service：

- `IPhotoAlbumService`
- `PhotoAlbumServiceImpl`

新增 Mapper：

- `IPhotoFolderMapper`
- `IPhotoImageMapper`

新增 Entity：

- `PhotoFolderEntity`
- `PhotoImageEntity`

新增 Req / VO：

- `PhotoFolderCreateReq`
- `PhotoFolderUpdateReq`
- `PhotoFolderDeleteReq`
- `PhotoFolderCoverReq`
- `PhotoImageQueryReq`
- `PhotoImageUpdateReq`
- `PhotoImageMoveReq`
- `PhotoImageDeleteReq`
- `PhotoFolderTreeVO`
- `PhotoImageUploadResultVO`

### 文件夹能力

支持：

- 查询当前用户文件夹树
- 新建文件夹
- 文件夹改名
- 删除空文件夹
- 多级文件夹
- 文件夹图片数量统计
- 文件夹封面图
- 手动设置封面
- 清除手动封面并自动回退最新图片

接口：

- `GET /photo-album/folder/tree`
- `POST /photo-album/folder/create`
- `POST /photo-album/folder/update`
- `POST /photo-album/folder/delete`
- `POST /photo-album/folder/cover/set`
- `POST /photo-album/folder/cover/clear`

### 图片能力

支持：

- 指定文件夹批量上传图片
- 按文件夹查询图片
- 搜索标题、备注、原始文件名
- 图片标题编辑
- 图片备注编辑
- 图片移动到其他文件夹
- 图片逻辑删除
- 受保护预览

接口：

- `POST /photo-album/image/upload-batch`
- `POST /photo-album/image/query`
- `POST /photo-album/image/update`
- `POST /photo-album/image/move`
- `POST /photo-album/image/delete`
- `GET /photo-album/image/preview?id=xxx`

### 权限规则

- 所有文件夹和图片按当前登录用户隔离。
- 上传、查询、预览、编辑、移动、删除、设置封面都校验用户归属。
- 图片预览不直接暴露 MinIO 公共地址，统一走受保护接口。
- 删除图片只做逻辑删除，不物理删除 MinIO 对象。
- 删除文件夹第一阶段只允许删除空文件夹。

### MinIO 存储

图片存储路径规则：

```text
photo-album/{userId}/{folderId}/{yyyyMMdd}/{uuid}.{ext}
```

默认桶：

```text
aiolife
```

允许图片类型：

- `image/jpeg`
- `image/png`
- `image/webp`
- `image/gif`

## 数据库变更

新增 SQL：

- `sql/2026-07-02.sql`

新增表：

- `photo_folder`
- `photo_image`

`photo_folder` 关键字段：

- `id`
- `user_id`
- `parent_id`
- `name`
- `sort_order`
- `cover_image_id`
- `create_user`
- `create_time`
- `update_user`
- `update_time`
- `is_deleted`

`photo_image` 关键字段：

- `id`
- `user_id`
- `folder_id`
- `bucket_name`
- `object_key`
- `original_filename`
- `content_type`
- `file_size`
- `title`
- `caption`
- `sort_order`
- `create_user`
- `create_time`
- `update_user`
- `update_time`
- `is_deleted`

新增菜单：

- 父菜单：`parent_id=1500`
- 路径：`/my-hub/photo-album`
- 组件：`my-hub/photo-album/index`
- 标题：`相册`
- 菜单 id：`1511`

### SQL 修复说明

原 `2026-07-02.sql` 曾出现中文注释和 COMMENT 编码损坏，导致脚本无法直接执行。

已修复为：

- ASCII 注释
- MySQL 兼容的幂等字段补充语句
- 菜单标题使用 `CONVERT(0xE79BB8E5868C USING utf8mb4)` 写入，避免命令行编码影响

该 SQL 已在本地库 `aio-life` 执行并验证：

- `photo_folder` 存在
- `photo_image` 存在
- 菜单 `/my-hub/photo-album` 存在
- 菜单标题为 UTF-8 `相册`

## 上传大小配置

为支持真实照片上传，新增 multipart 配置：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: ${AIO_LIFE_MULTIPART_MAX_FILE_SIZE:50MB}
      max-request-size: ${AIO_LIFE_MULTIPART_MAX_REQUEST_SIZE:300MB}
```

原因：

- Spring Boot 默认单文件上传上限为 `1MB`。
- 手机照片和旅游照片通常超过 `1MB`。
- 超限时请求会在进入 Controller 前被 multipart 解析器拒绝，前端表现为“网络异常”。

配置更新后，后端已重启验证。

## 前端相册页面

新增 API：

- `apps/web-antd/src/api/core/photo-album.ts`

新增页面：

- `apps/web-antd/src/views/my-hub/photo-album/index.vue`

### 桌面端体验

支持：

- 左侧文件夹树
- 右侧图片工具栏
- 搜索
- 批量上传
- 响应式大图网格
- 分页
- 图片悬浮操作层

图片卡片操作：

- 编辑
- 移动
- 设为封面
- 删除

### 手机端体验

支持：

- 顶部文件夹选择
- 当前路径展示
- 单列 / 双列图片流切换
- 默认双列大图流
- 悬浮上传按钮
- 触底加载更多
- 底部预览操作栏

### 图片懒加载

图片列表不再一次性加载当前页所有 Blob。

改为：

- 使用 `IntersectionObserver` 监听图片卡片进入视口
- 图片进入视口附近后再请求 `/photo-album/image/preview?id=xxx`
- 列表刷新或组件卸载时释放 `object URL`
- 预览时预加载当前图、上一张、下一张

### 空状态

支持三类空状态：

- 无文件夹：展示“新建第一个相册”
- 当前文件夹无图片：展示“上传照片”
- 搜索无结果：展示“清空搜索”

## 图片预览优化

阶段三完成了相册内沉浸式预览能力：

- 黑色背景预览
- 左右切换
- 放大
- 缩小
- 重置缩放
- 下载保存
- 查看图片信息
- 编辑
- 删除
- 大图 loading
- 加载失败重试

当前实现属于“沉浸式弹窗全屏”，视觉上铺满页面。

后续如果要做到类似桌面图片查看器，应采用：

- 独立图片查看器页面
- 浏览器原生 Fullscreen API
- 鼠标滚轮缩放
- 拖拽平移
- 键盘左右切换

建议后续页面：

```text
/my-hub/photo-album/preview?id=xxx
```

## 验证记录

后端：

- 已执行 `sql/2026-07-02.sql`
- 已确认 `photo_folder`、`photo_image` 表存在
- 已确认菜单 `/my-hub/photo-album` 存在
- 已确认 MinIO 健康检查正常
- 已确认 2MB multipart 请求不再被上传大小限制拦截
- 已重启后端并加载 multipart 配置

前端：

- `git diff --check` 已通过相册相关文件检查
- `pnpm --filter @vben/web-antd typecheck` 仍存在仓库既有错误
- 已确认 typecheck 输出中没有 `photo-album` 相关错误

## 已知限制

当前阶段暂不包含：

- EXIF 信息解析
- 地图定位
- 时间轴
- 服务端缩略图
- 分享链接
- AI 自动分类
- 图片物理删除
- 鼠标滚轮缩放
- 拖拽平移
- 独立图片查看器页面

## 后续建议

下一阶段建议做“独立图片查看器页面”：

- 路由：`/my-hub/photo-album/preview`
- 支持原生全屏
- 支持滚轮缩放
- 支持拖拽平移
- 支持键盘 `← / → / Esc`
- 支持图片底部状态栏
- 支持更接近桌面图片查看器的白底/黑底切换
