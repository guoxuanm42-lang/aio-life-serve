# AIO-LIFE 题目记录 MCP Agent 使用说明

## 工具

- `problem_note_query`：查询当前登录用户题库。
- `problem_note_save`：新增当前登录用户题目记录。

## 调用原则

当用户询问题库历史、某类题做过哪些、某个关键词相关题目、复习题目、已有解法或思路时，调用 `problem_note_query`。

仅当用户明确要求保存、记录、写入、新增题目时，才调用 `problem_note_save`。如果用户只是讨论算法、让你解题、让你优化代码，或明确说“先别保存”“不要入库”，不要调用写入工具。

## `problem_note_query` 入参建议

- `keyword`：从题目标题、题面关键词、算法标签或思路关键词中提取。
- `difficulty`：只有用户明确指定难度时传入。
- `status`：仅传 `draft` / `solved` / `reviewing` / `archived`。
- `tags`：用于按标签模糊查询，例如 `字符串`、`哈希表`。
- `categoryId`：用户明确指定分类 ID 时传入。
- `uncategorized`：用户明确要求未分类题目时传 `true`。
- `page` / `pageSize`：默认可不传；需要翻页时传入。

## `problem_note_save` 入参建议

- `title`：必填，题目标题。
- `problemContent`：必填，题目内容，可使用 Markdown。
- `solutionCode`：可选，仅保存 Java 解法代码。
- `ideaNote`：可选，保存解题思路、复杂度、易错点或复盘。
- `difficulty`：可选，例如 `easy` / `medium` / `hard` 或用户自定义难度文本。
- `tags`：可选，逗号分隔，例如 `数组,哈希表`。
- `status`：可选，默认 `draft`。
- `categoryId`：可选；为空时保存为未分类。
- `idempotencyKey`：建议按“题目标题 + 题目内容摘要 + 本轮会话稳定标识”生成，避免重复写入。

## 写入后回复

工具调用成功后，用简短中文回复用户：

```text
已保存到题目记录：<题目标题>
```

如果工具返回错误，应说明失败原因，不要声称已经保存。
