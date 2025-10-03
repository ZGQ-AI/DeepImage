# DeepImage

一个基于 Spring Boot 和 Vue 3 的现代化图片管理系统，支持文件上传、标签管理、文件分享等功能。

## 项目结构

```
DeepImage/
├── Deep-Image-Server/     # Spring Boot 后端服务
└── Deep-Image-Client/     # Vue 3 前端应用
```

## 技术栈

### 后端
- **Spring Boot 3.x** - 企业级 Java 应用框架
- **MyBatis Plus** - 持久层增强框架
- **Sa-Token** - 轻量级权限认证框架
- **PostgreSQL** - 关系型数据库
- **MinIO** - 对象存储服务
- **Google OAuth 2.0** - 第三方登录认证

### 前端
- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - 类型安全的 JavaScript 超集
- **Vite** - 下一代前端构建工具
- **Pinia** - Vue 官方状态管理库
- **Vue Router** - 官方路由管理器

## 功能特性

- ✅ 用户认证（Google OAuth 2.0）
- ✅ 文件上传与管理
- ✅ 标签系统
- ✅ 文件分享
- ✅ 访问日志记录
- ✅ 文件类型过滤
- ✅ 权限管理（RBAC）
- ✅ 会话管理
- ✅ 响应式设计

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- MinIO (可选，用于对象存储)
- pnpm

### 后端配置

1. 进入后端目录：
```bash
cd Deep-Image-Server
```

2. 复制配置文件：
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

3. 修改 `application.yml` 配置：
   - 数据库连接信息
   - Sa-Token JWT 密钥
   - Google OAuth 客户端 ID 和密钥
   - MinIO 连接信息

4. 执行数据库初始化脚本：
```bash
psql -U postgres -d deepimage -f src/main/resources/db/00001auth.sql
psql -U postgres -d deepimage -f src/main/resources/db/00002file.sql
```

5. 启动后端服务：
```bash
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 前端配置

1. 进入前端目录：
```bash
cd Deep-Image-Client
```

2. 安装依赖：
```bash
pnpm install
```

3. 启动开发服务器：
```bash
pnpm dev
```

前端应用将在 `http://localhost:5173` 启动。

## 开发文档

- [认证系统设计](Deep-Image-Server/docs/auth.md)
- [文件 API 设计](Deep-Image-Server/docs/file-api-design.md)
- [开发规范](Deep-Image-Server/docs/development-guidelines.md)

## API 文档

后端服务启动后，API 文档可通过以下方式访问：

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 项目架构

### 后端架构

```
org.tech.ai.deepimage/
├── config/          # 配置类
├── constant/        # 常量定义
├── controller/      # 控制器层
├── entity/          # 实体类
├── enums/           # 枚举类型
├── exception/       # 异常处理
├── mapper/          # MyBatis 映射器
├── model/           # DTO 模型
│   ├── dto/request/    # 请求 DTO
│   └── dto/response/   # 响应 DTO
├── service/         # 服务层接口
│   └── impl/           # 服务层实现
└── util/            # 工具类
```

### 前端架构

```
src/
├── api/             # API 接口定义
├── components/      # 可复用组件
├── layout/          # 布局组件
├── pages/           # 页面组件
├── router/          # 路由配置
├── stores/          # Pinia 状态管理
├── types/           # TypeScript 类型定义
├── utils/           # 工具函数
└── views/           # 视图组件
```

## 构建部署

### 后端构建

```bash
cd Deep-Image-Server
./mvnw clean package
java -jar target/DeepImage-0.0.1-SNAPSHOT.jar
```

### 前端构建

```bash
cd Deep-Image-Client
pnpm build
```

构建产物位于 `Deep-Image-Client/dist` 目录，可部署到任何静态服务器。

## 环境变量

### 后端环境变量

```bash
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

## 安全说明

⚠️ **重要提示**：
- 不要将 `application.yml` 提交到版本控制
- 生产环境务必修改默认密钥和密码
- 定期更新依赖包以修复安全漏洞
- 使用强密码和密钥
- 启用 HTTPS

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交 Pull Request

## 联系方式

如有问题或建议，欢迎通过 Issue 联系我们。

