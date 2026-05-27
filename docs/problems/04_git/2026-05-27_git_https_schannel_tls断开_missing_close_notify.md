# 2026-05-27_git_https_schannel_tls断开_missing_close_notify

## 1. 现象

- 执行 `git pull --rebase origin prod-serve` 失败：
  - `fatal: unable to access 'https://github.com/...': schannel: server closed abruptly (missing close_notify)`

---

## 2. 根因

- Windows 下 Git 默认使用 schannel 作为 TLS 后端。
- 在特定网络/代理/安全软件（HTTPS 扫描）环境下，与 GitHub 的 TLS 连接可能被中途断开，表现为 `missing close_notify`。

---

## 3. 解决方法

- 切换 Git 的 SSL 后端为 OpenSSL 并关闭吊销检查后重试：

```bash
git config --global http.sslBackend openssl
git config --global http.schannelCheckRevoke false
git pull --rebase origin prod-serve
```

- 如仍失败，改用 SSH 远端（绕开 HTTPS/TLS）：

```bash
git remote set-url origin git@github.com:guoxuanm42-lang/aio-life-serve.git
git pull --rebase origin prod-serve
```

