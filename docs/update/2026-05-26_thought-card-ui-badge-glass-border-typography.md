# 闪念｜卡片 UI（徽章、玻璃边框、排版）（2026-05-26）

## 1. 右上角徽章：纯 CSS 简易图标

### 1.1 变更内容
- 不再使用静态图片作为徽章图标
- 徽章保持玻璃拟态圆角方块风格，并跟随主题色产生柔光
- 徽章内部图标根据 `themeKey` 通过 CSS 绘制简易线条图形（工作/生活/学习/社交/创作/旅行）

### 1.2 修改原因
- 彩色 PNG 做 mask/上色容易出现“整块被染色成方块”的异常效果
- 纯 CSS 图标更可控，避免透明边距、缩放、上色等适配成本

---

## 2. 双层玻璃边框 + 排版微调

### 2.1 双层玻璃边框
- 外圈：卡片最外沿玻璃描边 + 主题色柔光 + 内高光
- 内圈：内容区域内再套一圈玻璃描边（默认可见，不依赖 hover）
- 移动端：降低辉光强度，避免显脏或显厚

### 2.2 排版微调
- 左侧主图标放大（桌面与移动端同步调整）
- 类型小标签（pill）放大并上移一点
- 标题字号加大，内容预览字号减小，形成层级对比

---

## 3. 技术实现细节

- 文件：`aio-life-front-main/aio-life-front-main/apps/web-antd/src/views/my-hub/think/list.vue`
  - 徽章：
    - DOM 透传 `data-theme`（值为 `blue/cyan/green/pink/purple/orange`）
    - `.protocol-badge`：玻璃拟态底座（背景渐变 + 内高光 + 主题色柔光 + 高光扫光）
    - `.protocol-badge::before`：使用多重 `linear-gradient/radial-gradient` 组合画出 pictogram
  - 边框：
    - `.thought-card`：外圈玻璃边框（border + 多重 box-shadow）
    - `.thought-card :deep(.ant-card-body)::after`：内圈玻璃边框（默认可见）
  - 排版：
    - `.protocol-icon / .protocol-icon-img`：左侧主图标尺寸调整
    - `.protocol-pill`：类型小标签尺寸/位置调整（包含 zoom 兼容）
    - `.protocol-title / .protocol-desc`：标题/内容字号调整

---

## 4. 本地验证
- 打开 `/think/all`：
  - 徽章不再触发 `/thought-icons/*.png` 的请求
  - 不同主题色的卡片徽章图标形态不同，且发光颜色随主题色变化
  - 卡片外圈与内圈边框均可清晰识别，标题/内容层级清晰

