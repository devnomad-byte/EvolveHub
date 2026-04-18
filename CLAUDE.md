# EvolveHub Project Specification

## Project Overview

EvolveHub is an AI-powered intelligent platform with macOS desktop-style UI, RBAC permission system, AI chat, knowledge base, and MCP services.

- **Java 21** + **Spring Boot 3.5.9** backend
- **Vue 3** + **TypeScript** + **Vite 8** frontend
- **PostgreSQL 16** + **Redis 7** + **MinIO** infrastructure

---

## Development Workflow

### Core Principle: ACT FIRST, verify later

1. **Trust CLAUDE.md as source of truth** — patterns, conventions, and architecture are documented here. Follow them directly without re-reading source files.
2. **Do NOT read files you already know** — if the pattern is clear from this doc, write code directly. Only read files when dealing with unfamiliar code or debugging.
3. **Do NOT use explore agents for known patterns** — agents are for genuinely unknown territory. A standard CRUD operation does NOT need exploration.
4. **Batch file operations** — create/modify all files in a single response. Don't read one file, respond, read another.
5. **No documentation files** — never create .md files unless explicitly asked.
6. **Single-shot implementation** — given a feature request, produce all files at once: Entity → Infra → Manager → Controller → API → Component → SQL. No step-by-step.

### Standard Backend CRUD Template

Given a new entity, produce these files in one shot:

```
domain-resource/model/XxxEntity.java      — extends BaseEntity, @TableName("eh_xxx")
domain-resource/infra/XxxInfra.java       — extends ServiceImpl<Mapper, Entity>
admin/service/CreateXxxManager.java       — extends BaseManager
admin/service/UpdateXxxManager.java       — extends BaseManager
admin/service/DeleteXxxManager.java       — extends BaseManager
admin/service/ListXxxManager.java         — extends BaseManager
admin/api/XxxController.java              — @RestController, @RequestMapping
admin/request/CreateXxxRequest.java       — Java record
admin/request/UpdateXxxRequest.java       — Java record
```

### Standard Frontend App Template

Given a new app, modify/create these files in one shot:

```
src/types/index.ts           — add to AppId union
src/types/apps.ts            — add to appDefinitions
src/api/xxx.ts               — types + API methods
src/components/apps/xxx/XxxApp.vue — full component
src/components/window/AppWindow.vue — add import + v-else-if
```

---

## Module Architecture

```
evolvehub-common          # Base classes, configs, utilities (library)
evolvehub-domain-rbac     # RBAC entities + Infra + UserDataScopeService (library)
evolvehub-domain-resource # Resource + Chat entities + Infra (library)
evolvehub-gateway         # API Gateway (port 8080)
evolvehub-auth            # Auth & User/Dept management (port 8081, context-path: /api/auth)
evolvehub-ai              # AI Platform: Chat, Memory, MCP runtime (port 8082, context-path: /api/ai)
evolvehub-admin           # Admin management: RBAC, Models, Chat history, Token usage (port 8083, context-path: /api/admin)
```

### Dependency Flow

```
gateway -> (no domain deps)
auth     -> common, domain-rbac
ai       -> common, domain-rbac, domain-resource
admin    -> common, domain-rbac, domain-resource
```

### Where to put what

| Type | Location | Example |
|------|----------|---------|
| Entity + Infra (shared data) | `domain-resource/model/` + `infra/` | ChatSessionEntity, ChatTokenUsageInfra |
| RBAC entity + Infra | `domain-rbac/model/` + `infra/` | UsersEntity, DeptInfra |
| Cross-cutting service (only rbac deps) | `domain-rbac/service/` | UserDataScopeService |
| Admin CRUD controller + manager | `admin/api/` + `admin/service/` | AdminChatHistoryController |
| User-facing controller + manager | `ai/api/` + `ai/service/` | UserChatController |
| Request DTO | Same module as the controller | `admin/request/`, `ai/request/` |

---

## Backend Conventions

### Layered Architecture

```
Controller -> Manager (extends BaseManager<REQ, RES>) -> Infra (extends ServiceImpl<Mapper, Entity>)
```

### Key Base Classes

| Class | Purpose |
|-------|---------|
| `BaseEntity` | Snowflake ID, createBy, createTime, updateTime, deleted |
| `BaseManager<REQ, RES>` | Template: `check()` -> `process()` -> `execute()` |
| `Result<T>` | `{code, message, data, timestamp}` — use `Result.ok(data)` / `Result.fail(msg)` |
| `PageRequest` | record: `pageNum` (default 1), `pageSize` (default 10, max 100) |
| `PageResponse<T>` | record: `records`, `total`, `pageNum`, `pageSize` |

### Entity Rules

- Extend `BaseEntity`, table prefix `eh_`, `@TableName("eh_xxx")`
- Snowflake ID: `@TableId(type = IdType.ASSIGN_ID)`
- Auto-fill: `@TableField(fill = FieldFill.INSERT)` / `FieldFill.INSERT_UPDATE`

### Manager Naming

`{Verb}{Noun}Manager` — one per operation: CreateXxxManager, ListXxxManager, etc.

### Infra Pattern

```java
@Repository
public class XxxInfra extends ServiceImpl<XxxInfra.XxxMapper, XxxEntity> {
    @Mapper interface XxxMapper extends BaseMapper<XxxEntity> {}
    // queries via lambdaQuery() / lambdaUpdate()
}
```

### Data Scope

`UserDataScopeService.getDataScopeInfo(userId)` returns `DataScopeInfo(dataScope, deptId, visibleDeptIds)`. Pass directly to Infra — no ThreadLocal.

Levels: 1=all, 2=dept+children, 3=dept only, 4=self, 5=custom

### Auth

Sa-Token, header `satoken`, `StpUtil.getLoginIdAsLong()` for user ID.

---

## Frontend Conventions

### Stack

Vue 3 `<script setup>` + TypeScript + Pinia + hand-written CSS (NO UI library). Icons: Lucide Vue Next. Animations: GSAP. HTTP: Axios `baseURL: '/api'`.

### Style

Glass morphism: `rgba(255,255,255,0.03-0.06)` bg, `backdrop-filter: blur(40px)`, `--border-subtle`, `--text-primary/secondary/disabled`. macOS colors: `#0A84FF` `#30D158` `#FF453A` `#BF5AF2` `#FFD60A` `#FF9F0A` `#64D2FF`.

### API Pattern

```typescript
import { http } from '@/utils/request'
export const xxxApi = {
  list: () => http.get('/admin/xxx/list'),
  create: (data) => http.post('/admin/xxx/create', data),
}
```

Routing: `/auth/...` → 8081, `/admin/...` → 8083, `/ai/...` → 8082

### Stores

`useDesktopStore` — auth/user state. `useWindowStore` — window lifecycle.

---

## Database

PostgreSQL 16, single DB `evolve_hub`. Table prefix `eh_`. Snowflake IDs. Logical delete `deleted`. Audit: `create_by`, `create_time`, `update_time`.

Role IDs: 1=SUPER_ADMIN, 2=LEADER, 3=DEPT_HEAD, 4=ADMIN, 5=USER

### Menu SQL

```sql
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES (N, 0, 'Name', 'app:xxx', 'MENU', '/app/xxx', 'Icon', sort, 1, NOW(), NOW(), 'linear-gradient(...)', W, H, MW, MH, -1, 1)
ON CONFLICT (id) DO NOTHING;
```

---

## Infrastructure

| Service | Port | DB |
|---------|------|----|
| Gateway | 8080 | - |
| Auth | 8081 | `localhost:5432/evolve_hub` (postgres/Postgres@2026) |
| AI | 8082 | same |
| Admin | 8083 | same |
| Frontend | 5178 | - |
| Redis | 6379 | pass: Redis@2026 |
| MinIO | 9000 | admin/minioadmin |

Vite proxy: `/api/auth`→8081, `/api/admin`→8083, `/api/ai`→8082, `/api/app`→8083

Docker infra: `docker/docker-compose.yml`. SQL: `backend/docs/sql/99-all-in-one.sql`. Execute SQL via: `docker exec evolvehub-postgres psql -U postgres -d evolve_hub -c "..."`

---

## Critical Rules

1. **Trust conventions, don't re-verify** — follow patterns from this doc directly
2. **No duplicate classes** — entities/infra live in domain modules, not in service modules
3. **No ThreadLocal for data scope** — use `UserDataScopeService` with direct params
4. **No .md files** unless explicitly requested
5. **Manager pattern** — one `XxxManager extends BaseManager` per operation
6. **Infra pattern** — `ServiceImpl<Mapper, Entity>` with `lambdaQuery()`
7. **Result wrapper** — all APIs return `Result<T>`
8. **No third-party UI library** — hand-written CSS only
9. **context-path in application.yml** — controller paths do NOT include `/api/xxx`
10. **Admin features go in admin module (8083)** — not in AI module. Shared entities go in domain-resource.
