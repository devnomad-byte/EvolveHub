# EvolveHub 模块划分与架构说明（MVP）

本文档基于《EvolveHub MVP 产品设计文档》，描述当前 Maven 工程的模块划分和整体架构。

## 一、整体工程结构

```
EvolveHub/
├── docs/                                    # 📄 文档与 SQL 脚本
│   ├── sql/
│   │   └── evolve_auth.sql                  # Auth RBAC 建表脚本 (PostgreSQL)
│   └── architecture-and-modules.md
│
├── docker/                                  # 🐳 容器化部署
│   ├── docker-compose.yml                   # PG 16 + Redis 7 + Nginx 1.27
│   ├── postgres/init.sql                    # 自动创建 evolve_auth / evolve_ai / evolve_admin
│   ├── redis/redis.conf
│   └── nginx/nginx.conf                     # 反向代理 → 8080/8081/8082/8083
│
├── service/                                 # 🚀 后端服务（Maven 聚合根）
│   ├── pom.xml                              # 父 POM (Spring Boot 3.5.9 + JDK 21)
│   ├── evolvehub-common/                    # 公共基础模块 (Jar)
│   ├── evolvehub-gateway/                   # API 网关 (:8080)
│   ├── evolvehub-auth/                      # 认证服务 (:8081)
│   ├── evolvehub-ai/                        # AI 平台服务 (:8082)
│   └── evolvehub-admin/                     # 管理服务 (:8083)
│
└── web/                                     # 🖥️ 前端应用（待初始化）
```

### 技术栈概览

| 层面 | 技术选型 |
|------|----------|
| 语言 / 运行时 | Java 21 / Spring Boot 3.5.9 |
| 数据库 | PostgreSQL 16 |
| 缓存 | Redis 7 |
| ORM | MyBatis-Plus 3.5.7 |
| 认证 | Sa-Token 1.38.0 |
| AI Agent | AgentScope 1.0.7 |
| 工具库 | Hutool 5.8.40 |
| 容器化 | Docker Compose |

### 包内分层规范

每个业务包统一采用以下子包分层：

| 子包 | 职责 |
|------|------|
| `model` | 领域模型 / 实体类 |
| `service` | 业务逻辑服务 |
| `infra` | 基础设施层（数据库、缓存、外部接口等） |
| `request` | 入参 DTO |
| `response` | 出参 DTO |
| `controller` | HTTP 接口（仅业务模块顶层） |

> 并非所有业务包都需要全部子包，按实际需要取用。

### 构建配置

- 根 pom 通过 `<pluginManagement>` 声明 `spring-boot-maven-plugin`
- 4 个可运行模块（`evolvehub-gateway`、`evolvehub-auth`、`evolvehub-ai`、`evolvehub-admin`）在各自 pom 的 `<plugins>` 中引用该插件
- 第三方依赖版本通过根 pom `<dependencyManagement>` 统一管理（AgentScope、MyBatis-Plus、Hutool、Sa-Token）
- Spring Boot 体系依赖版本由 `spring-boot-starter-parent` 自动管理，根 pom 中不重复声明

## 二、模块职责划分

### 1. evolvehub-common — 公共基础模块

包前缀：`org.evolve.common`

- `common.base`：统一父类 `BaseEntity`（id / createTime / updateTime / deleted 自动填充）、`MybatisPlusMetaHandler`
- `common.web`：统一返回体、全局异常处理、分页等 Web 通用组件
- `common.security`：RBAC 工具类、权限校验辅助、通用注解
- `common.storage`：MinIO、Milvus、Redis、数据库等存储客户端封装
- `common.observability`：日志规范、TraceId、metrics 相关工具

该模块不包含业务领域模型，只提供跨服务复用的技术基础设施。

### 2. evolvehub-gateway — API 网关

包前缀：`org.evolve.gateway`
端口：`8080`
启动类：`org.evolve.gateway.EvolveGatewayApplication`

职责：
- 统一入口，所有外部请求经过网关路由到各业务服务
- Sa-Token 网关级 Token 校验（与 Auth 共享 Redis Token 存储）
- CORS 统一配置
- 限流 / 熔断（后续扩展）

关键包：
- `gateway.config`：Sa-Token、CORS、Redis 等网关配置
- `gateway.filter`：请求过滤、Token 校验、路由转发

### 3. evolvehub-auth — 认证服务

包前缀：`org.evolve.auth`
端口：`8081`
启动类：`org.evolve.auth.api.AuthApiApplication`

职责：
- 用户登录 / 登出、Token 颁发与校验（Sa-Token）
- 用户 / 角色 / 权限 / 部门管理（RBAC + 数据权限）

关键包：
- `auth.api`：启动类
- `auth.api.controller`：登录、登出、鉴权等 HTTP 接口
- `auth.identity`：登录、Token 相关领域服务（含 `model` / `service` / `infra` / `request` / `response`）
- `auth.user`：用户、角色、权限、部门领域（含 `model` / `service` / `infra` / `request` / `response`）
- `auth.config`：Sa-Token、Redis 等配置

RBAC 实体（7 张表）：

| 实体类 | 表名 | 关键字段 |
|--------|------|----------|
| `UsersEntity` | `t_user` | `username`, `password`, `nickname`, `email`, `phone`, `avatar`, `deptId`, `status` |
| `DeptEntity` | `t_dept` | `parentId`, `deptName`, `sort`, `status` |
| `RolesEntity` | `t_role` | `roleName`, `roleCode`, `dataScope`(1-全部/2-本部门及子部门/3-仅本部门/4-仅本人/5-自定义), `sort`, `status` |
| `PermissionsEntity` | `t_permission` | `parentId`, `permName`, `permCode`, `permType`(MENU/BUTTON/API), `path`, `icon`, `sort`, `status` |
| `UserRolesEntity` | `t_user_role` | `userId`, `roleId` |
| `RolePermissionsEntity` | `t_role_permission` | `roleId`, `permissionId` |
| `RoleDataScopeEntity` | `t_role_data_scope` | `roleId`, `deptId`（dataScope=5 时生效） |

### 4. evolvehub-ai — AI 平台服务

包前缀：`org.evolve.aiplatform`
端口：`8082`
启动类：`org.evolve.aiplatform.AiPlatformApiApplication`

职责：
- 对话接口（REST + SSE），会话管理
- 基于 AgentScope 的 ReAct Agent 编排
- 短期 / 长期记忆管理（Redis + Milvus + PostgreSQL）
- 知识库在线检索（向量检索 + 文档切片）
- MCP 工具注册与调用

关键包：
- `aiplatform.controller`：REST / SSE 接口
- `aiplatform.conversation`：会话管理（含 `model` / `service` / `infra`）
- `aiplatform.agentcore`：ReAct Agent 核心流程、LLM 调用（含 `model` / `service`）
- `aiplatform.memory`：Redis 短期记忆、Milvus + PG 长期记忆（含 `model` / `service` / `infra`）
- `aiplatform.knowledgeruntime`：知识检索、向量召回与重排（含 `model` / `service`）
- `aiplatform.toolmcp`：MCP 工具注册表读取、调用（含 `model` / `service`）
- `aiplatform.config`：Milvus、MinIO、Redis 等配置

### 5. evolvehub-admin — 管理服务

包前缀：`org.evolve.admin`
端口：`8083`
启动类：`org.evolve.admin.AdminApiApplication`

职责：
- 部门 / 用户 / 角色后台管理
- 知识库、文档、分片、权限配置（GLOBAL / DEPT / PROJECT / SENSITIVE）
- 模型配置（LLM / Embedding）、MCP 工具注册管理
- 审计日志查看与基础监控配置

关键包：
- `admin.controller`：后台管理 HTTP 接口
- `admin.kb`：知识库管理（含 `model` / `service` / `infra` / `request` / `response`）
- `admin.userdept`：部门、用户管理（含 `model` / `service` / `infra` / `request` / `response`）
- `admin.modelconfig`：模型配置管理（含 `model` / `service` / `infra` / `request` / `response`）
- `admin.auditmonitor`：审计日志（含 `model` / `service` / `infra`）
- `admin.config`：MyBatis 等配置

Admin 不直接参与在线推理，对话侧通过数据库 / 配置读取其结果。

## 三、基础设施与部署

### Docker Compose 一键启动

```bash
cd docker
docker compose up -d
```

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| PostgreSQL | `postgres:16-alpine` | 5432 | 自动创建 3 个库 |
| Redis | `redis:7-alpine` | 6379 | AOF 持久化，256MB LRU |
| Nginx | `nginx:1.27-alpine` | 80 | 反向代理到 Gateway 及各服务 |

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_HOST` | `localhost` | PostgreSQL 地址 |
| `DB_USERNAME` | `postgres` | 数据库用户名 |
| `DB_PASSWORD` | `postgres` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | （空） | Redis 密码 |

## 四、模块间依赖约束

- `evolvehub-auth`、`evolvehub-ai`、`evolvehub-admin` **不要相互依赖**，仅依赖 `evolvehub-common`
- `evolvehub-common` 不放具体业务领域模型，仅放技术通用能力
- `evolvehub-gateway` 不依赖任何业务模块，仅通过 HTTP 转发
- 设计 API 时假定将来会通过网络调用，避免过深的代码级耦合
