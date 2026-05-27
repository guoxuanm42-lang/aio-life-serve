<!--
-- Maven 测试阶段 BUILD FAILURE（target 缓存导致 SpringBootTest 启动失败）
-- 创建时间: 2026-05-26
-- 作者: Ethan
-- 更新功能简介:
-- 1) 记录一次 mvn test 由于 target/classes 产物不一致导致的 ApplicationContext 启动失败排查与处理方式
-->

# Maven 测试阶段 BUILD FAILURE（target 缓存导致 SpringBootTest 启动失败）

## 1. 问题现象
执行 `mvn test` / `mvn -e test` 时出现：
- `BUILD FAILURE`
- `Failed to load ApplicationContext`
- `NoClassDefFoundError`（例如提示找不到某个 Mapper/类型）

## 2. 发生场景
在代码调整后，直接运行 `mvn test`，测试启动 Spring 容器失败。

## 3. 原因分析
本质是 **编译产物（target/classes）与源码不一致** 导致：
- Maven 增量编译误判为“无需重新编译”，继续使用旧的/脏的 class
- SpringBootTest 反射加载 Bean 时触发 `NoClassDefFoundError`

这类问题常见触发条件：
- 多次增量编译/切换分支/改动包结构后未清理 target
- IDE/命令行混合编译导致时间戳与产物状态异常

## 4. 解决方案
强制清理并重新编译（推荐）：
- `mvn clean test`

若只需要打包、不跑测试：
- `mvn -DskipTests clean package`

## 5. 验证方法
- 清理后重新执行 `mvn test`，确认不再出现 ApplicationContext 启动失败与 `NoClassDefFoundError`。

## 6. 注意事项
- 遇到“测试启动 Spring 容器失败 + 类型找不到”的组合，第一优先级就是 `mvn clean` 清 target，再做进一步排查。
