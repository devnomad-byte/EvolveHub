# EvolveHub RBAC 权限控制方案

## 一、整体架构

```
前端请求
   │
   ▼
┌──────────────────────────────┐
│  Gateway (8080)              │
│  ┌────────────────────────┐  │
│  │ SaReactorFilter        │  │
│  │ · 统一登录校验          │  │
│  │ · 白名单放行            │  │
│  │ · 未登录返回 401        │  │
│  └────────────────────────┘  │
│  路由转发:                    │
│  /api/auth/** → :8081        │
│  /api/ai/**   → :8082        │
│  /api/admin/** → :8083       │
└──────────────────────────────┘
   │
   ▼
┌──────────────────────────────┐
│  Auth Service (8081)         │
│  ┌────────────────────────┐  │
│  │ SaInterceptor          │  │
│  │ · 启用 @SaCheckXxx 注解│  │
│  ├────────────────────────┤  │
│  │ DataScopeWebInterceptor│  │
│  │ · 计算数据权限上下文    │  │
│  ├────────────────────────┤  │
│  │ DataScopeInterceptor   │  │
│  │ · MyBatis SQL 自动追加  │  │
│  │   数据过滤条件          │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

权限控制分为 **两个层面**：

| 层面 | 作用 | 实现位置 |
|------|------|----------|
| **功能权限**（接口级） | 控制用户能不能调用某个接口 | Gateway 统一登录校验 + 各 Controller `@SaCheckPermission` 注解 |
| **数据权限**（行级） | 控制用户能看到哪些数据 | MyBatis 拦截器自动追加 SQL WHERE 条件 |

---

## 二、数据模型（7 张表）

```
t_user ──┐
         │ N:N
t_role ──┤──── t_user_role (关联表)
         │
         ├──── t_role_permission (关联表) ──── t_permission
         │
         └──── t_role_data_scope (关联表) ──── t_dept
```

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `t_user` | 用户表 | username, password, dept_id, status |
| `t_role` | 角色表 | role_code, **data_scope**(1-5), status |
| `t_permission` | 权限表 | **perm_code**(如`user:list`), type(MENU/BUTTON/API) |
| `t_dept` | 部门表 | dept_name, parent_id (树形结构) |
| `t_user_role` | 用户-角色关联 | user_id, role_id |
| `t_role_permission` | 角色-权限关联 | role_id, permission_id |
| `t_role_data_scope` | 角色-数据范围关联 | role_id, dept_id (仅 data_scope=5 时使用) |

---

## 三、功能权限（接口级鉴权）

### 3.1 工作原理

```
用户请求 → Gateway (checkLogin) → 路由转发 → Auth Service
                                                │
                                    SaInterceptor 拦截
                                                │
                                    检测 @SaCheckPermission("user:list")
                                                │
                                    调用 StpInterfaceImpl.getPermissionList(userId)
                                                │
                                    查询链路: userId → t_user_role → t_role_permission → t_permission.permCode
                                                │
                                    比对用户权限码列表是否包含 "user:list"
                                                │
                                    ✓ 放行  /  ✗ 抛出 NotPermissionException
```

### 3.2 核心组件

#### StpInterfaceImpl（Sa-Token 权限数据提供者）

**位置**: `evolvehub-auth/.../service/StpInterfaceImpl.java`

| 方法 | 返回值 | 查询链路 |
|------|--------|----------|
| `getPermissionList(loginId)` | `["user:list", "user:create", ...]` | userId → t_user_role → t_role_permission → t_permission.permCode |
| `getRoleList(loginId)` | `["ROLE_ADMIN", "ROLE_USER"]` | userId → t_user_role → t_role.roleCode |

#### 权限码清单

| Controller | 接口 | 权限码 |
|------------|------|--------|
| **UserController** | POST /user/create | `user:create` |
| | GET /user/{id} | `user:query` |
| | GET /user/list | `user:list` |
| | PUT /user/update | `user:update` |
| | DELETE /user/{id} | `user:delete` |
| | POST /user/assign-role | `user:assign-role` |
| | POST /user/remove-role | `user:assign-role` |
| **RoleController** | POST /role/create | `role:create` |
| | GET /role/{id} | `role:query` |
| | GET /role/list | `role:list` |
| | PUT /role/update | `role:update` |
| | DELETE /role/{id} | `role:delete` |
| | POST /role/assign-permission | `role:assign-permission` |
| | POST /role/remove-permission | `role:assign-permission` |
| | POST /role/assign-data-scope | `role:assign-data-scope` |
| **PermissionController** | POST /permission/create | `permission:create` |
| | GET /permission/{id} | `permission:query` |
| | GET /permission/list | `permission:list` |
| | PUT /permission/update | `permission:update` |
| | DELETE /permission/{id} | `permission:delete` |
| **DeptController** | POST /dept/create | `dept:create` |
| | GET /dept/{id} | `dept:query` |
| | GET /dept/list | `dept:list` |
| | PUT /dept/update | `dept:update` |
| | DELETE /dept/{id} | `dept:delete` |

### 3.3 网关白名单

以下接口无需登录即可访问：

- `/api/auth/login` — 登录
- `/api/auth/register` — 注册
- `/actuator/**` — 健康检查

---

## 四、数据权限（行级过滤）

### 4.1 dataScope 类型说明

角色的 `data_scope` 字段控制该角色持有者的数据可见范围：

| data_scope 值 | 含义 | SQL 过滤效果 |
|---------------|------|-------------|
| **1** | 全部数据 | 不追加任何过滤条件 |
| **2** | 本部门及子部门 | `WHERE dept_id IN (自己部门ID, 子部门ID...)` |
| **3** | 仅本部门 | `WHERE dept_id IN (自己部门ID)` |
| **4** | 仅本人数据 | `WHERE create_by = 当前用户ID` |
| **5** | 自定义部门 | `WHERE dept_id IN (t_role_data_scope 中配置的部门ID列表)` |

### 4.2 多角色策略

当用户拥有多个角色时，取 **最大权限**（scopeType 值最小的角色优先）：

- 如果任一角色 data_scope=1 → 直接全部可见，无需过滤
- 否则取最小的 scopeType，同时合并所有角色的可见部门集合

### 4.3 工作流程

```
请求进入
   │
   ▼
DataScopeWebInterceptor.preHandle()
   │
   ├── 获取当前用户 ID (StpUtil.getLoginIdAsLong())
   ├── 查询用户所属部门
   ├── 查询用户所有角色
   ├── 遍历角色，计算最终 scopeType + 可见部门集合
   └── 存入 DataScopeContextHolder (ThreadLocal)
   │
   ▼
Controller → Manager → Infra.listPage()
   │
   ├── DataScopeContextHolder.enableFilter()  ← 显式开启过滤
   ├── MyBatis 执行 SQL
   │      │
   │      ▼
   │   DataScopeInterceptor.beforeQuery()
   │      │
   │      ├── 检查 isFilterEnabled() → true
   │      ├── 读取 DataScopeContext
   │      ├── 根据 scopeType 构建 SQL 条件
   │      └── 追加到原始 SQL 的 WHERE 子句
   │
   └── DataScopeContextHolder.disableFilter()  ← 关闭过滤
   │
   ▼
DataScopeWebInterceptor.afterCompletion()
   └── DataScopeContextHolder.clear()  ← 清除 ThreadLocal
```

### 4.4 关键设计：显式开关

**问题**：如果对所有 SELECT 都追加数据过滤，会影响 `StpInterfaceImpl` 查权限、`DataScopeWebInterceptor` 查角色/部门等内部查询（这些表没有 `dept_id` 列，会 SQL 报错）。

**解决方案**：引入 `DataScopeContextHolder.enableFilter()` / `disableFilter()` 开关，仅在需要数据过滤的分页列表查询中主动开启：

```java
// UsersInfra.listPage()
public Page<UsersEntity> listPage(int pageNum, int pageSize) {
    DataScopeContextHolder.enableFilter();  // 开启过滤
    try {
        return this.page(new Page<>(pageNum, pageSize));
    } finally {
        DataScopeContextHolder.disableFilter();  // 确保关闭
    }
}
```

目前启用了数据权限过滤的查询：
- `UsersInfra.listPage()` — 用户列表
- `DeptInfra.listPage()` — 部门列表

### 4.5 create_by 自动填充

`BaseEntity` 中新增了 `createBy` 字段，由 `MybatisPlusMetaHandler` 在插入时自动填充当前登录用户 ID（通过 `CurrentUserHolder` 获取），用于 `data_scope=4`（仅本人数据）的过滤。

---

## 五、涉及文件清单

### 新增文件

| 文件 | 模块 | 说明 |
|------|------|------|
| `StpInterfaceImpl.java` | auth | Sa-Token 权限/角色数据提供者 |
| `DataScopeWebInterceptor.java` | auth | Web 拦截器，计算数据权限上下文 |
| `WebMvcConfig.java` | auth | 注册 SaInterceptor + DataScopeWebInterceptor |
| `DataScope.java` | common | `@DataScope` 注解（预留，可标注 Mapper 方法） |
| `DataScopeContext.java` | common | 数据权限上下文对象 |
| `DataScopeContextHolder.java` | common | ThreadLocal 持有者 + 显式开关 |
| `DataScopeInterceptor.java` | common | MyBatis-Plus InnerInterceptor，SQL 自动追加 |
| `CurrentUserHolder.java` | common | 通用当前用户 ID 持有者（解耦 Sa-Token） |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `BaseEntity.java` | 新增 `createBy` 字段 |
| `MybatisPlusMetaHandler.java` | 插入时自动填充 `createBy` |
| `MybatisPlusConfig.java` | 注册 `DataScopeInterceptor`（在分页插件之前） |
| `UserController.java` | 所有方法加 `@SaCheckPermission` |
| `RoleController.java` | 所有方法加 `@SaCheckPermission` |
| `PermissionController.java` | 所有方法加 `@SaCheckPermission` |
| `DeptController.java` | 所有方法加 `@SaCheckPermission` |
| `UsersInfra.java` | `listPage()` 中启用数据权限过滤 |
| `DeptInfra.java` | `listPage()` 中启用数据权限过滤 |
| `UserRolesInfra.java` | 新增 `listByUserId()` 查询方法 |
| `RolePermissionsInfra.java` | 新增 `listByRoleId()` 查询方法 |

---

## 六、使用指南

### 6.1 新增接口如何接入功能权限

1. 在 `t_permission` 表中录入权限记录，如 `perm_code = "article:publish"`
2. 在 Controller 方法上加注解：
   ```java
   @SaCheckPermission("article:publish")
   @PostMapping("/publish")
   public Result<Void> publish(...) { ... }
   ```
3. 通过管理接口为角色分配该权限

### 6.2 新增查询如何接入数据权限

1. 确保查询的表有 `dept_id` 或 `create_by` 字段
2. 在 Infra 的列表查询方法中加入显式开关：
   ```java
   public Page<XxxEntity> listPage(int pageNum, int pageSize) {
       DataScopeContextHolder.enableFilter();
       try {
           return this.page(new Page<>(pageNum, pageSize));
       } finally {
           DataScopeContextHolder.disableFilter();
       }
   }
   ```

### 6.3 数据库准备

需要确保 `t_user`、`t_dept` 等业务表包含 `create_by` 列（BIGINT），用于 `data_scope=4`（仅本人数据）过滤。

```sql
ALTER TABLE t_user ADD COLUMN create_by BIGINT;
ALTER TABLE t_role ADD COLUMN create_by BIGINT;
ALTER TABLE t_permission ADD COLUMN create_by BIGINT;
ALTER TABLE t_dept ADD COLUMN create_by BIGINT;
```

**注意**：BCrypt 密码必须使用 Hutool 的 `BCrypt.hashpw()` 生成，与 Python bcrypt 库生成的哈希不兼容。

## 七、登录注册接口

| 接口 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `POST /api/auth/login` | `POST` | 用户登录，返回 token + 用户信息 + 角色/权限列表 | 白名单（无需 token） |
| `POST /api/auth/register` | `POST` | 用户注册，用户名/邮箱/手机号全局唯一 | 白名单（无需 token） |
| `POST /api/auth/logout` | `POST` | 用户登出，清除 Sa-Token 会话 | 需 token |

## 八、测试验证结果

| 测试项 | 场景 | 预期 | 实际结果 |
|--------|------|------|----------|
| **网关拦截** | 未登录访问 `/api/auth/user/list` | 401 | ✅ `{"code":401,"msg":"未能读取到有效 token"}` |
| **白名单放行** | 访问 `/api/auth/login` | 200 | ✅ 登录接口正常响应 |
| **注解鉴权-未登录** | 直连 8081 访问 `/user/list` | 401 | ✅ `{"code":401,"message":"未登录或Token已过期"}` |
| **注解鉴权-有权限** | admin 访问 `user/list` | 200 | ✅ 返回 3 条用户记录（全部数据） |
| **注解鉴权-无权限** | zhangsan 访问 `user/create` | 403 | ✅ `{"code":403,"message":"缺少权限: user:create"}` |
| **登录接口** | admin 登录 | 200 + token | ✅ 返回 token + 12 个权限 + ROLE_ADMIN 角色 |
| **登录接口** | zhangsan 登录 | 200 + token | ✅ 返回 4 个权限 + ROLE_TECH_MGR 角色 |
| **登录接口** | lisi 登录 | 200 + token | ✅ 返回 2 个权限 + ROLE_STAFF 角色 |
| **数据权限-scope1** | admin(全部数据) 访问 `user/list` | 3 条 | ✅ 看到 admin、zhangsan、lisi 共 3 人 |
| **数据权限-scope2** | zhangsan(本部门及子部门) 访问 `user/list` | 1 条 | ✅ 只看到自己（技术部 dept_id=2） |
| **数据权限-scope4** | lisi(仅本人) 访问 `user/list` | 1 条 | ✅ 只看到自己（`create_by=300`） |
| **create_by 自动填充** | admin 创建用户 | create_by=100 | ✅ 新创建用户 `create_by` 自动填充为当前用户 ID |
