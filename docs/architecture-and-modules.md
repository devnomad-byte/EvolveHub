# EvolveHub 模块划分与架构说明（MVP）

本文档基于《EvolveHub MVP 产品设计文档》，描述当前 Maven 工程的模块划分和整体架构，便于后续演进为微服务。

## 一、整体工程结构

根项目：`evolve`（`pom` 聚合工程，Spring Boot 3.5.9 + JDK 21）

- `common`：公共基础模块（Jar）
- `auth`：认证与权限聚合模块（`auth-service` + `auth-gateway`）
- `ai-platform`：对话 / Agent / 记忆 / 知识检索聚合模块（`ai-platform-core` + `ai-platform-api`）
- `admin`：后台管理 / 知识库管理聚合模块（`admin-service` + `admin-api`）

当前为**多模块单体**部署，后续可以按模块拆分为独立微服务。

### 包内分层规范

每个业务包统一采用以下子包分层：

| 子包 | 职责 |
|------|------|
| `model` | 领域模型 / 实体类 |
| `service` | 业务逻辑服务 |
| `infra` | 基础设施层（数据库、缓存、外部接口等） |
| `request` | 入参 DTO |
| `response` | 出参 DTO |

> 并非所有业务包都需要全部 5 个子包，按实际需要取用。例如 `config` 包通常不需要分层。

### 构建配置

- 根 pom 通过 `<pluginManagement>` 声明 `spring-boot-maven-plugin`
- 只有 3 个可运行模块（`auth-gateway`、`ai-platform-api`、`admin-api`）在各自 pom 的 `<plugins>` 中引用该插件
- 第三方依赖版本通过根 pom `<dependencyManagement>` 统一管理（AgentScope、MyBatis-Plus、Hutool、Sa-Token）
- Spring Boot 体系依赖版本由 `spring-boot-starter-parent` 自动管理，根 pom 中不重复声明

## 二、模块职责划分

### 1. common 模块

包前缀：`org.evolve.common`

- `common.web`：统一返回体、全局异常处理、分页等 Web 通用组件
  - `web.exception`：全局异常处理
  - `web.response`：统一返回体
  - `web.page`：分页工具
- `common.security`：RBAC 工具类、权限校验辅助、通用注解
  - `security.rbac`：角色权限工具
  - `security.annotation`：权限注解
- `common.storage`：MinIO、Milvus、Redis、数据库等存储客户端封装
  - `storage.minio`：MinIO 对象存储
  - `storage.milvus`：Milvus 向量数据库
  - `storage.redis`：Redis 缓存
- `common.observability`：日志规范、TraceId、metrics 相关工具
  - `observability.log`：日志规范
  - `observability.metrics`：指标采集

该模块不包含业务领域模型，只提供跨服务复用的技术基础设施。

### 2. auth 模块（认证与权限）

包前缀：`org.evolve.auth`  
端口（建议）：`8081`

职责（整体）：
- 用户登录 / 登出、Token 颁发与校验（Sa-Token）  
- 用户 / 角色 / 权限管理  
- 作为其他服务调用的统一认证入口

`auth` 本身是聚合模块，内部再拆为两个实际子模块：

- `auth-service`（核心认证服务 Jar）：  
  - 包前缀：`org.evolve.auth`  
  - 主要职责：认证领域逻辑、用户 / 角色 / 权限管理、持久化  
  - 关键包：
    - `auth.identity`：登录、登出、Token 相关领域服务（含 `model` / `service` / `infra` / `request` / `response`）  
    - `auth.user`：用户、角色、权限等领域模型与服务（含 `model` / `service` / `infra` / `request` / `response`）  

- `auth-gateway`（对外网关 / API Jar，可单独启动）：  
  - 启动类：`org.evolve.auth.gateway.AuthGatewayApplication`  
  - 主要职责：HTTP 接口、网关过滤、与 Sa-Token 集成  
  - 关键包：
    - `auth.gateway.controller`：登录、登出、鉴权等 HTTP 接口  
    - `auth.gateway.request`：网关层入参 DTO  
    - `auth.gateway.response`：网关层出参 DTO  
    - `auth.config`：Sa-Token、Redis、安全相关配置  

未来拆分为独立微服务时，一般将 `auth-gateway` 作为独立进程部署，`auth-service` 作为内部依赖或进一步拆分。

### 3. ai-platform 模块（对话与 Agent 平台）

包前缀：`org.evolve.aiplatform`  
端口（建议）：`8082`

职责（整体）：
- 对话接口（REST + SSE），会话管理  
- 基于 AgentScope 的 ReAct Agent 编排  
- 短期 / 长期记忆管理（Redis + Milvus + MySQL）  
- 知识库在线检索（向量检索 + 文档切片）  
- MCP 工具注册与调用

`ai-platform` 本身是聚合模块，内部拆为：

- `ai-platform-core`（对话 / Agent / 记忆核心 Jar）：  
  - 包前缀：`org.evolve.aiplatform`  
  - 关键包：
    - `aiplatform.conversation`：会话管理、会话存储等核心逻辑（含 `model` / `service` / `infra` / `request` / `response`）  
    - `aiplatform.agentcore`：ReAct Agent 核心流程、LLM 调用、工具决策（含 `model` / `service`）  
    - `aiplatform.memory`：Redis 短期记忆、Milvus + MySQL 长期记忆、MEMORY.md 处理（含 `model` / `service` / `infra`）  
    - `aiplatform.knowledgeruntime`：知识检索、向量召回与重排（含 `model` / `service`）  
    - `aiplatform.toolmcp`：MCP 工具注册表读取、调用与安全护栏（含 `model` / `service`）  
    - `aiplatform.config`：Milvus、MinIO、Redis 等基础设施配置  

- `ai-platform-api`（对话 API / SSE 网关 Jar，可单独启动）：  
  - 启动类：`org.evolve.aiplatform.AiPlatformApiApplication`  
  - 主要职责：对外暴露 REST + SSE 接口，调用 `ai-platform-core` 完成业务  
  - 关键包：
    - `aiplatform.controller`：REST / SSE 接口  
    - `aiplatform.request`：API 层入参 DTO  
    - `aiplatform.response`：API 层出参 DTO  

未来拆分为独立微服务时，一般将 `ai-platform-api` 作为对外服务进程部署，`ai-platform-core` 作为内部业务库。

### 4. admin 模块（后台管理与知识库）

包前缀：`org.evolve.admin`  
端口（建议）：`8083`

职责（整体）：
- 部门 / 用户 / 角色后台管理  
- 知识库、文档、分片、权限配置（GLOBAL / DEPT / PROJECT / SENSITIVE）  
- 模型配置（LLM / Embedding）、MCP 工具注册管理  
- 审计日志查看与基础监控配置

`admin` 本身是聚合模块，内部拆为：

- `admin-service`（后台核心业务 Jar）：  
  - 包前缀：`org.evolve.admin`  
  - 关键包：
    - `admin.kb`：知识库、文档、分片及权限的 CRUD 与状态管理（含 `model` / `service` / `infra` / `request` / `response`）  
    - `admin.userdept`：部门、用户、角色的后台管理逻辑（含 `model` / `service` / `infra` / `request` / `response`）  
    - `admin.modelconfig`：模型配置、Embedding 配置、MCP 工具配置管理（含 `model` / `service` / `infra` / `request` / `response`）  
    - `admin.auditmonitor`：审计日志、告警规则等运维相关功能（含 `model` / `service` / `infra`）  
    - `admin.config`：与后台业务相关的配置（如 MyBatis 等）  

- `admin-api`（后台管理 API Jar，可单独启动）：  
  - 启动类：`org.evolve.admin.AdminApiApplication`  
  - 主要职责：对外暴露后台管理 HTTP 接口，调用 `admin-service` 完成业务  
  - 关键包：
    - `admin.controller`：后台管理 HTTP 接口  
    - `admin.request`：API 层入参 DTO  
    - `admin.response`：API 层出参 DTO  

Admin 不直接参与在线推理，对话侧通过数据库 / 配置读取其结果。

## 三、与 MVP 架构的对应关系

- Auth Service（MVP 文档）：  
  - 对应当前 `auth` 聚合模块，主要由 `auth-gateway` + `auth-service` 共同实现。  
- AI Platform（MVP 文档）：  
  - 对应当前 `ai-platform` 聚合模块，主要由 `ai-platform-api` + `ai-platform-core` 共同实现。  
- Admin Service（MVP 文档）：  
  - 对应当前 `admin` 聚合模块，主要由 `admin-api` + `admin-service` 共同实现。

所有模块共享 `common` 中的技术基础设施和规范。

## 四、向微服务演进的路径

当前工程是「模块化单体」，模块边界已经对齐未来的微服务边界：

- 顶层服务边界：`auth`、`ai-platform`、`admin` 三个聚合模块；  
- 每个服务内部再次拆分为 `*-api`（对外服务进程） + `*-service` / `*-core`（内部业务库）。

向微服务演进时的典型做法：

- 将来拆分时，可以将 `auth-gateway`、`ai-platform-api`、`admin-api` 拎出为三个独立部署单元；  
- `auth-service`、`ai-platform-core`、`admin-service` 作为各自服务内部的业务模块（可保持为本地 Jar，也可以进一步拆成独立服务）；  
- `common` 保持为共享基础库，或按需要拆分 / 拷贝到各个服务中；  
- 服务间调用建议通过 HTTP / RPC（目前可以先在单体内部直接调用服务类，接口契约按微服务风格设计）。

只要保持以下约束，后续拆分成本较低：

- `auth`、`ai-platform`、`admin` **不要相互依赖**，仅依赖 `common`；  
- `*-api` 只通过公开接口调用各自的 `*-service` / `*-core`，避免直接穿透到底层实现；  
- `common` 不放具体业务领域模型，仅放技术通用能力；  
- 设计 API 时假定将来会通过网络调用，避免过深的代码级耦合。
