# 2026-06-25 文章模块 MCP 工具与文档收口

## 背景

文章模块前三阶段已完成后端数据闭环、前端基础页面、Markdown 阅读和编辑体验。本次收口第四阶段，让外部 AI 可以通过 MCP 查询、新增和查看文章详情，并补齐 Agent 使用文档与回归测试。

## 变更内容

- 新增 MCP 工具：
  - `article_query`：分页查询当前用户文章列表，只返回摘要和元信息。
  - `article_detail`：按文章 ID 查询详情，返回 Markdown 原文和纯文本内容。
  - `article_save`：新增文章，支持 `idempotencyKey` 防重复写入。
- 新增 MCP 专用请求和返回结构：
  - `ArticleQueryToolReq`
  - `ArticleDetailToolReq`
  - `ArticleSaveToolReq`
  - `ArticleQueryToolVO`
  - `ArticleDetailToolVO`
- 新增 `ArticleAiFacade`，复用文章服务层，保持 `plainTextContent` 和 `wordCount` 由后端生成。
- 新增 `docs/mcp/article-agent-usage.md`，说明工具字段、触发规则和使用方式。
- 更新 MCP README 和调用原则文档，补充文章工具。

## 测试

- 新增 `ArticleMcpToolsTest`，验证 MCP 保存请求到业务保存请求的字段映射。
- 新增 `ArticleAiFacadeTest`，验证查询、详情、幂等命中、首次创建和无幂等键创建。
- 更新 `McpToolCompatibilityTest`，校验文章工具名和 schema 字段。
- 更新 `McpSchemaGeneratorTest`，校验 `article_save` 和 `article_detail` 必填字段。

## 验证结果

- `mvn "-Dtest=ArticleMcpToolsTest,ArticleAiFacadeTest,McpToolCompatibilityTest,McpSchemaGeneratorTest,McpToolRegistryTest" test`：通过。
- `mvn -DskipTests compile`：通过。
- `pnpm --filter @vben/web-antd build`：失败，失败点为既有 `jiti@2.6.1` 引入 `node:module/createRequire` 被 Vite 浏览器外部化，不指向文章页面或文章 API。

## 说明

- `article_save` 第一版只支持新增，不支持更新已有文章。
- 分类只接收已有 `categoryId`，不会在 MCP 保存文章时自动创建分类。
- 前端构建当前仍可能受 monorepo 既有 `jiti/node:module` 打包问题影响；该问题与文章 MCP 后端工具无直接关系。
