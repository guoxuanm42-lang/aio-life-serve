# API Key 认证日志脱敏修复

## 背景

API Key 认证拦截器在凭证不存在或已过期时，会将请求中的完整 API Key 写入 warning 日志。如果日志文件被误公开、复制到外部系统，或日志查看权限过宽，完整凭证可能被泄露。

本次更新统一 API Key 的脱敏规则，确保认证日志、认证异常和 API Key 列表都不再输出完整凭证。

## 解决的问题

- 修复 API Key 不存在时日志输出完整 Key 的问题。
- 修复 API Key 已过期时日志输出完整 Key 的问题。
- 避免 `NotLoginException` 对象内保留完整 API Key，降低异常链或堆栈记录导致的泄露风险。
- 解决认证日志和列表展示分别维护脱敏代码，可能出现规则不一致的问题。
- 修复异常短值原样返回的边界情况。

## 脱敏规则

正常 API Key 保留前 8 位和后 4 位，中间统一替换为三个星号：

```text
ak-1234567890abcdef
        ↓
ak-12345***cdef
```

日志输出示例：

```text
API Key ak-12345***cdef 不存在
API Key ak-12345***cdef 已过期
```

对于以下异常输入，统一输出 `***`，不保留任何原始字符：

- `null`
- 空字符串
- 纯空格字符串
- 长度不足 12 位的异常 Key

## 本次调整

### 统一脱敏工具

新增：

- `top.aiolife.sso.util.ApiKeyMaskUtil`

提供统一方法：

```java
public static String mask(String apiKey)
```

认证日志和 API Key 列表转换都复用该方法，避免重复实现。

### API Key 认证拦截器

调整 `ApiKeyInterceptor`：

- API Key 不存在或已逻辑删除时，日志仅记录脱敏值。
- API Key 已过期时，日志仅记录脱敏值。
- 抛出 `NotLoginException` 时不再将完整 API Key 写入异常字段。
- 保持对外错误信息不变：`API Key 无效`、`API Key 已过期`。
- 保持 API Key 查询、过期校验、`StpUtil.switchTo` 身份切换和调用日志记录逻辑不变。

### API Key 列表转换

调整 `ApiKeyConvertor`：

- 移除转换器内的重复 substring 脱敏逻辑。
- 改为调用 `ApiKeyMaskUtil.mask`。
- 正常 API Key 的列表返回格式保持不变。

## 影响范围

本次是后端安全修复，不涉及：

- 数据库表结构变更
- API 请求或响应结构变更
- 前端页面和交互变更
- API Key 生成规则变更
- API Key 权限范围变更
- API Key 数据库存储方式变更

原有客户端和 MCP / AI Agent 接入方式保持兼容。

## 验证记录

新增测试：

- `ApiKeyMaskUtilTest`
  - 正常 API Key 脱敏。
  - 短 Key 完全隐藏。
  - `null`、空字符串和纯空格值完全隐藏。
- `ApiKeyInterceptorTest`
  - 不存在 Key 的日志和异常脱敏。
  - 已删除 Key 的日志和异常脱敏。
  - 已过期 Key 的日志和异常脱敏。
  - 有效 Key 的身份切换和请求放行逻辑保持不变。

已在 JDK 21 下执行：

```bash
mvn "-Dtest=ApiKeyMaskUtilTest,ApiKeyInterceptorTest" test
mvn test
```

验证结果：

- 定向测试：7 个测试全部通过。
- 后端全量测试：142 个测试全部通过。
- `git diff --check` 通过。

## 后续建议

本次仅解决日志和展示层的凭证泄露问题。后续可拆分独立任务继续完善：

- 将 API Key 改为哈希或前缀索引存储，避免数据库保存完整凭证。
- 增加 API Key 启用/禁用状态。
- 增加只读、MCP 专用和指定工具等权限范围。
- 生产环境强制通过 HTTPS 传输 API Key。
