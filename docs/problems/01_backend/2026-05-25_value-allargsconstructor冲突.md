<!--
-- @Value 和 @AllArgsConstructor 冲突
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 修复 Controller 新增 @Value 字段后导致 Spring 误注入 String Bean 的启动失败问题
-->

# @Value 和 @AllArgsConstructor 冲突

## 1. 问题现象
后端启动时报错：`required a bean of type 'java.lang.String' that could not be found`。

## 2. 发生场景
在 Controller 中新增 `@Value("${aio.life.serve.base-url}")` 字段后出现（例如 [ThoughtController.java](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/api/ThoughtController.java)）。

## 3. 原因分析
类上使用了 `@AllArgsConstructor`，Lombok 会把所有字段都放进构造方法，包括 `String` 类型的 `@Value` 字段。Spring 在创建 Bean 时会尝试通过构造器注入该 `String` 参数，但容器中不存在对应的 `String` Bean，导致启动失败。

## 4. 解决方案
将 `@AllArgsConstructor` 改为 `@RequiredArgsConstructor`，只对 `final` 依赖做构造注入，`@Value` 字段走字段注入。

本项目修复点：
- [ThoughtController.java](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/api/ThoughtController.java) 将 `@AllArgsConstructor` 替换为 `@RequiredArgsConstructor`

## 5. 验证方法
- 重启后端：`mvn -DskipTests spring-boot:run`
- 确认启动成功（`Started AioLifeMain`），接口可正常访问（例如 `http://localhost:45678/api`）。

## 6. 注意事项
- Controller 中混用 Lombok 构造器注入和 `@Value` 时要注意构造器生成范围。
- 建议统一使用 `@RequiredArgsConstructor` + `final` 依赖注入风格，避免将非依赖字段误纳入构造器参数。

