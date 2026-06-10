# 问题索引（docs/problems）

本目录用于归档项目开发、部署、运维过程中遇到的问题记录，按领域分类存放，便于检索与复盘。

## 目录结构

- `01_backend/`：后端代码、框架、依赖、运行期问题
- `02_frontend/`：前端工程、构建、样式、浏览器兼容问题
- `03_database/`：数据库结构、迁移脚本、数据一致性问题
- `04_git/`：Git、GitHub、分支协作、拉取、推送、stash 等问题
- `04_tooling/`：MCP、Trae、命令行调试、开发工具链相关问题
- `05_server/`：服务器环境、网络、权限、系统服务问题
- `06_docker/`：Docker、Compose、镜像、容器相关问题
- `07_deployment/`：部署脚本、CI/CD、Nginx、发布流程问题
- `08_operations/`：日常操作、终端、IDE、常用工具链问题
- `09_security/`：密钥、权限、审计、敏感信息与安全问题

## 最近记录

| 日期 | 模块 | 问题主题 | 类型 | 文档 |
|---|---|---|---|---|
| 2026-06-10 | 04_tooling | MCP Streamable HTTP 手动调用缺少 session-id | problem | [查看文档](../04_tooling/2026-06-10_mcp-streamable-http手动调用缺少session-id.md) |
| 2026-06-09 | 01_backend | Spring Boot 后端重启失败：45678 端口被旧进程占用 | problem | [查看文档](../01_backend/2026-06-09_springboot后端重启失败_45678端口占用.md) |
| 2026-06-02 | 07_deployment | 服务器访问 analytics 进入项目 404：sys_menu 菜单被禁用 | problem | [查看文档](../07_deployment/2026-06-02_服务器访问analytics进入项目404_sys_menu菜单被禁用.md) |
| 2026-05-27 | 04_git | Git HTTPS schannel TLS 断开：missing close_notify | problem | [查看文档](../04_git/2026-05-27_git_https_schannel_tls断开_missing_close_notify.md) |
| 2026-05-27 | 04_git | Git 切分支提示 untracked 将被覆盖，无法 switch | problem | [查看文档](../04_git/2026-05-27_git切分支提示untracked将被覆盖_无法switch.md) |
| 2026-05-27 | 04_git | PowerShell 中 stash@{0} 大括号解析导致 Git 命令失败 | problem | [查看文档](../04_git/2026-05-27_powershell_git_stash_at大括号解析导致命令失败.md) |
| 2026-05-27 | 08_operations | 终端无法输入：误入多行模式或被程序占用 | problem | [查看文档](../08_operations/2026-05-27_终端无法输入_误入多行模式或被程序占用.md) |
| 2026-05-27 | 01_backend | Spring Boot 启动失败：NoClassDefFoundError CbtiPersonalitySaveReq | problem | [查看文档](../01_backend/2026-05-27_springboot启动失败_NoClassDefFoundError_CbtiPersonalitySaveReq.md) |
| 2026-05-27 | 01_backend | initial 01_schema.sql 在 prod 分支缺失，切分支看似文件丢失 | problem | [查看文档](../01_backend/2026-05-27_sql_initial_01_schema在prod分支缺失_切分支看似文件丢失.md) |
| 2026-05-26 | 02_frontend | CBTI 静态图片未加载：资源位置不一致 | problem | [查看文档](../02_frontend/2026-05-26_cbti静态图片未加载_资源位置不一致.md) |
| 2026-05-26 | 02_frontend | CSS 徽章 mask 导致方块 | problem | [查看文档](../02_frontend/2026-05-26_css徽章mask导致方块.md) |
| 2026-05-26 | 02_frontend | Vite 端口占用自动切换 | problem | [查看文档](../02_frontend/2026-05-26_vite端口占用自动切换.md) |
| 2026-05-26 | 03_database | thought 表缺少 status 字段 | problem | [查看文档](../03_database/2026-05-26_thought表缺少status字段.md) |
| 2026-05-26 | 01_backend | thought 状态保存不生效 | problem | [查看文档](../01_backend/2026-05-26_thought状态保存不生效.md) |
| 2026-05-26 | 01_backend | Maven 测试 BUILD FAILURE：target 缓存 | problem | [查看文档](../01_backend/2026-05-26_maven测试BUILD-FAILURE_target缓存.md) |
| 2026-05-25 | 03_database | thought 表缺少 card_object 字段 | problem | [查看文档](../03_database/2026-05-25_thought表缺少card_object字段.md) |
| 2026-05-25 | 01_backend | @Value 与 @AllArgsConstructor 冲突 | problem | [查看文档](../01_backend/2026-05-25_value-allargsconstructor冲突.md) |
