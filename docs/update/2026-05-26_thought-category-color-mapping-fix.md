# 闪念｜分类颜色映射修复（2026-05-26）

## 1. 修复的问题

### 1.1 学习/生活分类主题色映射错误
- 目标规则：
  - 学习：蓝色（blue）
  - 生活：绿色（green）
- 修复内容（前端分类与主题色映射）：
  - work=cyan
  - life=green
  - study=blue
  - social=pink
  - creation=purple
  - travel=orange

---

## 2. 修改原因
- 分类颜色是用户对“分类”的直觉记忆点；学习与生活颜色错位会造成识别成本与使用混乱。
- 本项目后端对 `themeKey` 有白名单约束，因此颜色修正必须保持在预设 key 内完成。

---

## 3. 技术实现细节

### 3.1 前端映射修复
- 文件：`aio-life-front-main/aio-life-front-main/apps/web-antd/src/views/my-hub/think/list.vue`
  - `categoryPresets` 内 life/study 的 `themeKey/accent/rgb` 调整

### 3.2 可选数据迁移（保持历史数据与新映射一致）
- 若历史闪念记录是按照旧映射写入（例如旧“学习=green、生活=cyan、工作=blue”），仅改前端会导致分类筛选和展示与历史数据错位。
- 提供迁移脚本：
  - `sql/2026-05-26_swap_think_theme_key_for_life_study_work.sql`
  - 规则：
    - blue → cyan
    - cyan → green
    - green → blue

---

## 4. 本地验证
- 切换到“学习/生活”分类页面：
  - 学习卡片应为蓝色系
  - 生活卡片应为绿色系
- 若执行迁移脚本：
  - 历史记录在对应分类下筛选应能正确命中

