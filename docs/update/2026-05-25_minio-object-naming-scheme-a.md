# 2026-05-25_minio-object-naming-scheme-a

## 1. 新增功能

### 1.1 头像存储统一为“单桶 + 前缀目录”
- 目标结构：`aiolife/avatar/{userId}/{uuid}.{ext}`
- 说明：
  - `aiolife` 为 bucket（默认桶），`avatar/...` 为对象键前缀（不是桶）
  - 预览接口仍为 `/file/preview/{bucket}/{objectName}`

### 1.2 闪念卡图片上传（think-card）
- 新增闪念卡图片对象键字段：`thought.card_object`
- 新增上传接口：`POST /thought/{id}/card/upload`（multipart/form-data，字段名 `file`）
- 目标结构：`aiolife/think-card/{thinkId}/{uuid}.{ext}`
- 返回：`cardObject` 与 `cardUrl`

---

## 2. 修复的问题

### 2.1 重启后端启动失败（ThoughtController String Bean 注入错误）
- 现象：
  - 执行 `mvn spring-boot:run` 启动失败，报错 `ThoughtController required a bean of type 'java.lang.String'`
- 原因：
  - `ThoughtController` 使用 `@AllArgsConstructor` 导致 `@Value` 注入字段参与构造器注入，Spring 误以为需要注入一个 `String` Bean
- 修复：
  - 将 `ThoughtController` 构造注入方式调整为 `@RequiredArgsConstructor`，只对 `final` 字段构造注入，`@Value` 保持字段注入

---

## 3. 修改原因

- 统一对象命名结构后，MinIO 控制台展示路径更清晰（按业务模块前缀聚类），后续迁移/治理更可控。
- 增加 `think-card` 图片上传能力，为闪念卡可视化（海报/缩略图/封面图）预留存储位。
- CBTI 图片从历史前缀 `images/cbti/characters/` 统一到 `cbti/`，便于与其它模块的前缀规则保持一致。

---

## 4. 技术实现细节

### 4.1 数据库变更
- 增量脚本：
  - `sql/2026-05-25_add_card_object_to_thought.sql`
    - `thought` 表新增：`card_object VARCHAR(512) NULL COMMENT 'MinIO 对象键（think-card/...）'`
  - `sql/2026-05-25_migrate_cbti_image_object_prefix.sql`
    - 将 `cbti_personality.image_object` 从 `images/cbti/characters/SUDO.png` 迁移为 `cbti/SUDO.png`（保留文件名/扩展名，仅替换前缀）
- 初始化脚本同步：
  - `sql/initial/01_schema.sql`
    - `thought` 表增加 `card_object`
    - `cbti_personality.image_object` 注释示例更新为 `cbti/SUDO.png`

### 4.2 后端接口与实体
- 文件：[UserController.java](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/sso/api/UserController.java#L141-L158)
  - `/users/avatar/upload`：
    - bucket：读取 `aio.life.serve.minio.bucket-name`（默认 `aiolife`）
    - objectName：`avatar/{userId}/{uuid}.{ext}`
- 文件：[ThoughtEntity.java](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/pojo/entity/ThoughtEntity.java#L26-L33)
  - 新增字段 `cardObject` 映射数据库列 `card_object`
- 文件：[ThoughtController.java](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/api/ThoughtController.java#L184-L236)
  - 新增 `/thought/{id}/card/upload`：
    - 校验：必须登录、thought 必须属于当前用户、仅支持 `image/*`
    - 写入：默认桶 `aiolife`，对象键 `think-card/{id}/{uuid}.{ext}`
    - 落库：写回 `thought.card_object`
- 文件：[CbtiConfig.java](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/config/CbtiConfig.java#L16-L27)
  - `objectPrefix` 默认值改为 `cbti/`
- 文件：[application.yml](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/resources/application.yml#L126-L131)
  - `aio.life.serve.cbti.object-prefix` 默认值改为 `cbti/`
- 文件：[CbtiAdminController.java](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/api/CbtiAdminController.java#L313-L317)
  - CBTI 前缀兜底常量同步为 `cbti/`

---

## 5. 本地验证

### 5.1 后端启动
- `mvn -DskipTests spring-boot:run`
- API：`http://localhost:45678/api`

### 5.2 验证点
- 头像上传后返回 URL 应包含：
  - `/file/preview/aiolife/avatar/{userId}/...`
- 闪念卡图片上传：
  - 调用 `POST /thought/{id}/card/upload` 成功后返回 `cardObject` 与 `cardUrl`
  - `cardUrl` 可直接访问并返回图片（200）
- CBTI 迁移后校验：
  - `cbti_personality.image_object` 变为 `cbti/{filename}`
  - 重启后观察 `CbtiInitRunner` 日志，确认会在 `cbti/` 前缀下补齐上传（前提：本地 `characters-dir` 存在同名图片文件）

