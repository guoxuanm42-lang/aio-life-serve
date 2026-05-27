# 2026-05-27_powershell_git_stash_at大括号解析导致命令失败

## 1. 现象

- 在 PowerShell 中执行以下命令时报错：
  - `git stash show --name-status stash@{0}`
  - `git stash apply stash@{0}`
- 典型报错：
  - `Too many revisions specified: 'stash@' ...`
  - `error: unknown switch 'e'`

---

## 2. 根因

- PowerShell 将 `stash@{0}` 里的 `@{}` 识别为哈希表/表达式语法并进行解析，导致参数被拆分传给 Git。

---

## 3. 解决方法

- 用引号包裹 stash 引用（推荐）：

```bash
git stash show --name-status "stash@{0}"
git stash apply "stash@{0}"
git stash pop "stash@{0}"
```

