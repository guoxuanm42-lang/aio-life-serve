# 2026-05-27_springboot启动失败_NoClassDefFoundError_CbtiPersonalitySaveReq

## 1. 现象

- 执行 `mvn spring-boot:run` 启动失败，进程退出码为 1。
- 日志出现：
  - `java.lang.NoClassDefFoundError: CbtiPersonalitySaveReq`
  - 触发点在 `ControllerMcpToolRegistry.init()`（扫描 Controller 并反射解析方法签名）。

---

## 2. 根因

- 工作区经过 `stash/切分支/恢复` 等操作后，编译产物（`target/`）处于不一致或不完整状态，导致运行时 classpath 中缺失 `CbtiPersonalitySaveReq.class`。
- MCP 工具注册逻辑在启动阶段扫描 `@RestController`，反射读取方法签名会触发参数类型加载，从而提前暴露缺失类问题并导致启动失败。

---

## 3. 解决方法

- 执行干净编译以重建 `target/classes`：

```bash
mvn -DskipTests clean compile
mvn -DskipTests spring-boot:run
```

