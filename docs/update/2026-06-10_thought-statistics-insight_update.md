# 闪念统计洞察与时间趋势更新（2026-06-10）

## 1. 背景与目标

本次更新围绕「闪念」模块新增统计洞察能力，让用户除了浏览闪念列表外，也能看到整体积累、处理状态、分类分布、时间趋势和活跃规律。

目标包括：

- 在闪念页面新增「统计洞察」Tab。
- 提供基础统计总览、状态分布、分类分布。
- 增加时间趋势、分类新增、活跃度和闪念爆发日分析。
- 优化统计页面层级，避免 9 个指标平铺造成信息密集。
- 修复「分类新增」图表 Y 轴异常放大的问题。

## 2. 更新内容

### 2.1 后端统计接口

新增闪念统计接口：

- `GET /thought/statistics/overview`
  - 返回基础统计总览、状态分布、分类分布。
- `GET /thought/statistics/trend`
  - 返回新增趋势、分类新增趋势、活跃度和爆发日。

新增/扩展的后端对象：

- `ThoughtStatisticsVO`
- `ThoughtStatisticsTrendVO`
- `ThoughtStatisticsTrendReq`
- `IThoughtStatisticsService`
- `ThoughtStatisticsServiceImpl`
- `ThoughtStatisticsController`

统计数据全部基于现有 `thought` 表实时聚合，不新增数据库字段和新表。

### 2.2 前端统计洞察页

闪念列表页新增：

- `记录列表`
- `统计洞察`

统计洞察页分为四块：

- 核心指标区
- 辅助统计区
- 分布分析区
- 趋势分析区

核心指标区展示：

- 总闪念数
- 待处理
- 本月新增
- 转化率

辅助统计区展示：

- 本周新增
- 已完成
- 已归档
- 积压数

不再单独展示「高价值闪念」，因为当前第一版中它等同于「已归档」，重复展示会增加页面噪音；后端字段 `highValueCount` 保留，便于后续真正引入价值评分或精华标记。

### 2.3 趋势分析

趋势分析区支持筛选：

- 时间范围：最近 7 天、最近 30 天、本月、本年。
- 分组方式：按日、按周、按月。
- 分类：全部分类、工作、生活、健康、学习、社交、创作、AIO-LIFE开发、旅行。
- 状态：全部状态、待处理、进行中、已完成、已搁置、已归档。

图表包括：

- 新增趋势：折线图。
- 分类新增：柱状图。
- 活跃度：简化柱状图。

如果存在闪念爆发日，则在趋势分析顶部显示紧凑提示条。

## 3. 字段维度与计算逻辑

### 3.1 基础统计字段

所有统计均限定：

- 当前登录用户。
- `isDeleted = 0`。
- 时间字段使用 `createTime`。

字段口径：

| 字段 | 计算逻辑 |
|---|---|
| `totalCount` | 当前用户全部未删除闪念数量 |
| `weekNewCount` | `createTime >= 本周一 00:00:00` 的数量 |
| `monthNewCount` | `createTime >= 本月 1 日 00:00:00` 的数量 |
| `pendingCount` | `status = pending` 的数量 |
| `doneCount` | `status = done` 的数量 |
| `archivedCount` | `status = archived` 的数量 |
| `conversionRate` | `doneCount / totalCount * 100`，无数据时为 `0`，保留 1 位小数 |
| `backlogCount` | `pendingCount + ongoingCount` |
| `highValueCount` | 第一版等同于 `archivedCount`，前端暂不独立展示 |

### 3.2 状态分布

状态分布固定返回以下状态，即使数量为 0 也返回：

| key | name |
|---|---|
| `pending` | 待处理 |
| `ongoing` | 进行中 |
| `done` | 已完成 |
| `shelved` | 已搁置 |
| `archived` | 已归档 |

每项字段：

- `key`：状态编码。
- `name`：状态中文名。
- `count`：该状态数量。
- `percent`：`count / totalCount * 100`，无数据时为 `0`，保留 1 位小数。

### 3.3 分类分布

分类基于 `themeKey` 聚合：

| themeKey | 分类 |
|---|---|
| `cyan` | 工作 |
| `green` | 生活 |
| `teal` | 健康 |
| `blue` | 学习 |
| `pink` | 社交 |
| `purple` | 创作 |
| `indigo` | AIO-LIFE开发 |
| `orange` | 旅行 |
| `unknown` | 未分类 |

空值或未识别的 `themeKey` 归为 `unknown=未分类`。

### 3.4 时间趋势

趋势接口参数：

| 参数 | 支持值 | 默认值 | 说明 |
|---|---|---|---|
| `range` | `7d`、`30d`、`month`、`year` | `30d` | 统计时间范围 |
| `groupBy` | `day`、`week`、`month` | `day` | 时间分组方式 |
| `category` | `themeKey` | 空 | 分类筛选 |
| `status` | 状态编码 | 空 | 状态筛选 |

时间范围规则：

- `7d`：包含今天，向前取 7 个自然日。
- `30d`：包含今天，向前取 30 个自然日。
- `month`：本月 1 日到今天。
- `year`：本年 1 月 1 日到今天。

分组规则：

- `day`：返回 `yyyy-MM-dd`。
- `week`：按周一归组，返回周一日期 `yyyy-MM-dd`。
- `month`：返回 `yyyy-MM`。

所有时间点都会补 0，保证折线图和柱状图连续展示。

### 3.5 分类新增

「分类新增」表示当前筛选条件下，按分类统计一段时间内新创建的闪念数量。

例如筛选为「最近 30 天 / 全部分类 / 全部状态」时，它表示：

- 最近 30 天工作新增多少条。
- 最近 30 天生活新增多少条。
- 最近 30 天健康新增多少条。
- 最近 30 天 AIO-LIFE开发新增多少条。

前端展示时会对每个分类的 `points[]` 做求和：

```text
分类新增总数 = sum(points[].count)
```

修复说明：

- 曾出现 Y 轴显示 `14,000,000,000,000` 的异常。
- 原因是前端聚合时可能把接口返回的 `count` 当作字符串参与 `+` 运算，导致字符串拼接。
- 已新增 `toSafeCount()`，在折线图、分类新增柱状图、活跃度柱状图渲染前统一将 `count` 转为安全数字。

### 3.6 闪念爆发日

爆发日按日统计，不受 `groupBy` 影响。

计算逻辑：

```text
平均日数量 = 当前 range 内每日数量总和 / 天数
爆发阈值 = max(3, 平均日数量 * 2)
当天数量 >= 爆发阈值，则视为闪念爆发日
```

返回字段：

- `date`：爆发日期。
- `count`：当天新增数量。
- `average`：当前时间范围内的平均日数量。

## 4. 图表与图片展示逻辑

### 4.1 图表展示逻辑

统计洞察页使用 ECharts 展示统计数据。

图表渲染规则：

- 状态分布图：环图。
  - 数据来源：`statusDistribution`。
  - 只展示 `count > 0` 的扇区。
  - 无有效数据时展示空态。
- 分类分布图：环图。
  - 数据来源：`categoryDistribution`。
  - 只展示 `count > 0` 的扇区。
  - 无有效数据时展示空态。
- 新增趋势图：折线图。
  - 数据来源：`trend`。
  - X 轴为 `date`。
  - Y 轴为 `count`。
  - 无有效数据时展示空态。
- 分类新增图：柱状图。
  - 数据来源：`categoryTrends`。
  - X 轴为 `categoryName`。
  - Y 轴为该分类 `points[].count` 的总和。
  - 渲染前使用 `toSafeCount()` 防止字符串拼接导致数轴异常。
- 活跃度图：柱状图。
  - 数据来源：`activity`。
  - X 轴为日期。
  - Y 轴为当天新增数量。

图表展示由后端返回的结构化统计数据驱动，前端只负责展示和必要的数值安全转换，不重新定义业务口径。

### 4.2 图片与图标展示逻辑

本次统计洞察不新增业务图片上传和图片存储。

相关图片/图标展示逻辑如下：

- 闪念列表卡片仍沿用现有静态分类图标：
  - `/thought-icons/work.png`
  - `/thought-icons/life.png`
  - `/thought-icons/healthy.png`
  - `/thought-icons/study.png`
  - `/thought-icons/social.png`
  - `/thought-icons/creation.png`
  - `/thought-icons/aio-life.png`
  - `/thought-icons/travel.png`
- 图标选择依据：
  - 优先读取闪念记录的 `themeKey`。
  - 根据 `themeKey` 映射到对应分类图标。
  - 未识别或为空时使用默认工作图标兜底。
- 统计洞察页本身不展示上传图片，核心展示对象是指标卡和 ECharts 图表。
- 图表不是图片文件，而是前端运行时基于数据绘制的 canvas/SVG 可视化。

## 5. 接口返回结构

### 5.1 `/thought/statistics/overview`

```json
{
  "summary": {
    "totalCount": 0,
    "weekNewCount": 0,
    "monthNewCount": 0,
    "pendingCount": 0,
    "doneCount": 0,
    "archivedCount": 0,
    "conversionRate": 0,
    "backlogCount": 0,
    "highValueCount": 0
  },
  "statusDistribution": [],
  "categoryDistribution": []
}
```

### 5.2 `/thought/statistics/trend`

```json
{
  "range": "30d",
  "groupBy": "day",
  "trend": [
    { "date": "2026-06-10", "count": 1 }
  ],
  "categoryTrends": [
    {
      "categoryKey": "indigo",
      "categoryName": "AIO-LIFE开发",
      "points": [
        { "date": "2026-06-10", "count": 1 }
      ]
    }
  ],
  "activity": [
    { "date": "2026-06-10", "count": 1 }
  ],
  "burstDays": [
    { "date": "2026-06-10", "count": 3, "average": 1.2 }
  ]
}
```

## 6. 验证情况

后端执行：

```bash
$env:JAVA_HOME="D:\my_app\jdk\java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -DskipTests compile
```

结果：

- 编译通过。

前端执行：

```bash
pnpm --filter @vben/web-antd typecheck
```

结果：

- 仍被仓库既有 TypeScript 问题阻断。
- 当前已确认新增的闪念统计相关代码没有出现在 typecheck 新增错误中。

## 7. 影响范围

- 不新增数据库字段。
- 不新增 SQL 脚本。
- 不影响闪念列表默认展示、分类筛选、状态筛选、主题搜索、新增、编辑和删除流程。
- 统计数据随新增、编辑、删除后清空缓存；当前位于统计洞察页时会重新加载。

## 8. 后续建议

- 若后续需要真正的「高价值闪念」，建议新增独立字段或标签，例如 `isHighValue`、价值评分、复盘沉淀标记等，不再复用 `archived`。
- 若数据量明显增长，可将当前内存聚合改为数据库聚合或增加统计缓存。
- 活跃度图后续可升级为日历热力图。
