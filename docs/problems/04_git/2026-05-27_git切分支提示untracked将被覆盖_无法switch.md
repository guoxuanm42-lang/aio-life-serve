# 2026-05-27_git切分支提示untracked将被覆盖_无法switch

## 1. 现象

- 执行 `git switch <branch>` / `git checkout <branch>` 被拒绝：
  - `error: The following untracked working tree files would be overwritten by checkout: ...`
- 常见场景：
  - 恢复了 `sql/initial/01_schema.sql` 等文件，但它们在当前分支并未被 Git 跟踪（untracked）。

---

## 2. 根因

- 目标分支上存在同路径文件（或将要检出同路径文件），而当前工作区有同名 untracked 文件。
- Git 为防止覆盖并丢失这些 untracked 文件而阻止切换。

---

## 3. 解决方法

- 方案 A：先把工作区收纳（包含未跟踪文件）

```bash
git stash push -a -m "wip"
git switch <branch>
git stash pop "stash@{0}"
```

- 方案 B：把 stash 内容恢复到新分支（避免覆盖冲突）

```bash
git stash branch wip-restore "stash@{0}"
```

