# AIO-LIFE 文章模块 MCP Agent 使用说明

## 工具

- `article_query`：查询当前登录用户文章列表，只返回摘要和元信息，不返回正文全文。
- `article_detail`：按文章 ID 查看当前登录用户文章详情，返回 Markdown 原文和纯文本内容。
- `article_save`：新增当前登录用户文章，支持 `idempotencyKey` 防重复写入。

## 调用原则

当用户询问已有文章、某类文章、某个关键词相关内容、历史草稿或文章元信息时，优先调用 `article_query`。

当用户需要查看某篇文章完整正文、继续编辑、分析已有文章内容或复制 Markdown 原文时，先通过 `article_query` 找到目标文章 ID，再调用 `article_detail`。

仅当用户明确要求“保存文章”“新增文章”“写入文章模块”“把这段整理成文章保存”等动作时，才调用 `article_save`。如果用户只是让你润色、讨论结构、生成草稿但没有要求入库，不要调用写入工具。

## `article_query` 入参建议

- `keyword`：从标题、摘要、正文关键词或标签中提取。
- `status`：仅传 `draft` / `published` / `archived`。
- `tags`：用于按标签模糊查询，例如 `Java,项目文档`。
- `categoryId`：用户明确指定分类 ID 时传入。
- `uncategorized`：用户明确要求未分类文章时传 `true`，并忽略 `categoryId`。
- `page` / `pageSize`：默认可不传；需要翻页时传入，`pageSize` 最大 200。

查询结果只包含 `id`、`categoryId`、`title`、`summary`、`tags`、`status`、`wordCount`、`createTime`、`updateTime`。如需正文，必须调用 `article_detail`。

## `article_detail` 入参建议

- `id`：必填，来自 `article_query` 返回结果或用户明确给出的文章 ID。

详情返回包含 `markdownContent` 和 `plainTextContent`。对外展示、复制、发布到 CSDN/公众号等场景，优先使用 `markdownContent`。

## `article_save` 入参建议

- `title`：必填，文章标题。
- `markdownContent`：必填，Markdown 原文正文。
- `summary`：可选；不传时后端不会自动生成摘要。
- `categoryId`：可选；为空时保存为未分类。
- `tags`：可选，英文逗号分隔，例如 `技术文章,Java,项目文档`。
- `status`：可选，支持 `draft` / `published` / `archived`，为空默认 `draft`。
- `idempotencyKey`：推荐传入，建议由“文章标题 + 正文摘要 + 会话稳定标识”生成，避免重试重复创建。

不要传 `plainTextContent` 或 `wordCount`。这两个字段由后端根据 Markdown 原文自动生成。

## 写入后回复

工具调用成功后，用简短中文回复用户：

```text
已保存到文章模块：<文章标题>
```

如果工具返回错误，应说明失败原因，不要声称已经保存。
