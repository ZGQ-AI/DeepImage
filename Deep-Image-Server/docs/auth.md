# Login
~~~mermaid
sequenceDiagram
    participant Client
    participant Browser
    participant Server
    participant Database

    Note over Client,Browser: 用户在浏览器输入邮箱/密码并点击登录
    Client->>Browser: 触发表单提交（校验必填/格式）

    Browser->>Server: POST /api/auth/login\nBody: { email|username, password }\nHeaders: { User-Agent }
    Server->>Server: 参数校验（非空、格式）
    Server->>Database: SELECT * FROM sys_users WHERE email = ? OR username = ?
    alt 用户存在且状态正常
        Server->>Server: PasswordUtil.matches(明文, password_hash)
        alt 密码正确
            Server->>Server: StpUtil.login(userId, SaLoginModel)\n(启用JWT，写入自定义claims: username/email)
            Server->>Server: 生成 accessToken(JWT)
            Server->>Server: 计算 access_token_hash（hash+盐）

            Server->>Database: INSERT INTO sys_sessions\n(user_id, access_token_hash, device_info, ip_address, user_agent,\n active, last_accessed_at, created_at, updated_at)\nVALUES (userId, accessHash, NULL, ip, ua, 1, now, now, now)

            Server->>Server: 生成 refreshToken 明文（高熵随机）
            Server->>Server: 计算 refresh_token_hash（hash+盐）
            Server->>Database: INSERT INTO sys_refresh_tokens\n(user_id, token_hash, session_id, expires_at, last_used_at, revoked,\n delete_flag, created_at, updated_at)\nVALUES (userId, rtHash, sessionId, now + TTL, NULL, 0, 0, now, now)

            opt 策略：登录时撤销旧RT（可配置）
                Server->>Database: UPDATE sys_refresh_tokens SET revoked=1, updated_at=now\nWHERE user_id = ? AND session_id <> ? AND revoked=0
            end

            Server-->>Browser: 200 OK\n{ accessToken: JWT, refreshToken: 明文 }
            Browser->>Client: 存储 accessToken（内存/Storage）\nrefreshToken（可 httpOnly Cookie）
        else 密码错误
            Server-->>Browser: 401 Unauthorized\n{ message: INVALID_CREDENTIALS }
        end
    else 用户不存在/被禁用/未验证
        Server-->>Browser: 403 Forbidden\n{ message: FORBIDDEN }
    end
~~~