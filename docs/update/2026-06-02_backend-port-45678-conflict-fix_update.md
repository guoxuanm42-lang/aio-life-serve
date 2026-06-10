# 后端启动端口 45678 占用处理记录（2026-06-02）

## 1. 问题现象

在后端项目目录执行：

```powershell
mvn spring-boot:run
```

启动失败，日志提示：

```text
Identify and stop the process that's listening on port 45678
or configure this application to listen on another port.
```

随后 Maven 报错：

```text
Failed to execute goal org.springframework.boot:spring-boot-maven-plugin:3.3.10:run
Process terminated with exit code: 1
```

---

## 2. 根因

本地 `45678` 端口已经被旧的 Java 后端进程占用。

Spring Boot 默认仍尝试监听同一个端口，因此新进程启动失败。

这不是代码编译问题，也不是 Maven 插件问题，而是运行环境中已有进程占用了服务端口。

---

## 3. 排查过程

### 3.1 查看端口占用

PowerShell 中执行：

```powershell
netstat -ano | Select-String ":45678"
```

结果显示：

```text
TCP    0.0.0.0:45678    0.0.0.0:0    LISTENING    16340
TCP    [::]:45678       [::]:0        LISTENING    16340
```

说明 PID `16340` 正在监听 `45678`。

### 3.2 验证旧服务是否仍可响应

请求本地接口：

```powershell
Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:45678/api/menu/all" -TimeoutSec 5
```

返回 `401 未经授权`，说明端口上的服务确实是一个正在运行的后端服务，而不是无效占用。

---

## 4. 处理方式

### 4.1 停止旧进程

```powershell
Stop-Process -Id 16340 -Force
```

再次检查端口：

```powershell
netstat -ano | Select-String ":45678"
```

无输出，说明端口已经释放。

### 4.2 使用 JDK 21 重新启动后端

项目使用 Java 21，需要先设置 `JAVA_HOME`：

```powershell
$env:JAVA_HOME="D:\my_app\jdk\java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

后台启动后端：

```powershell
Start-Process -FilePath "mvn" `
  -ArgumentList @("spring-boot:run") `
  -WorkingDirectory "d:\my_document\program_product\life_os\aio-life-serve-main\aio-life-serve-main" `
  -RedirectStandardOutput "d:\my_document\program_product\life_os\backend-run.log" `
  -RedirectStandardError "d:\my_document\program_product\life_os\backend-run.err.log" `
  -WindowStyle Hidden
```

---

## 5. 验证结果

端口重新监听：

```text
TCP    0.0.0.0:45678    0.0.0.0:0    LISTENING    20696
TCP    [::]:45678       [::]:0        LISTENING    20696
```

启动日志显示：

```text
Started AioLifeMain
ReadinessState changed to ACCEPTING_TRAFFIC
```

说明后端已重新启动成功。

日志文件：

```text
d:\my_document\program_product\life_os\backend-run.log
d:\my_document\program_product\life_os\backend-run.err.log
```

---

## 6. 后续建议

- 如果只是想确认服务是否已启动，优先访问 `http://localhost:45678/api` 或查看 `backend-run.log`，不需要重复启动。
- 如果确实需要重启，先用 `netstat -ano | Select-String ":45678"` 找到旧进程并停止。
- 如需并行运行两个后端实例，应修改其中一个实例的服务端口，而不是复用 `45678`。
