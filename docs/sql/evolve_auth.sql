-- ============================================================
-- EvolveHub Auth 模块 - RBAC + 数据权限建表脚本
-- 数据库：evolve_auth (PostgreSQL)
-- ============================================================

-- -----------------------------------------------------------
-- 1. 部门表（树形结构，支撑数据权限）
-- -----------------------------------------------------------
CREATE TABLE t_dept (
    id          BIGINT       NOT NULL,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    dept_name   VARCHAR(64)  NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL,
    update_time TIMESTAMP    NOT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX idx_dept_parent_id ON t_dept (parent_id);
COMMENT ON TABLE  t_dept IS '部门表';
COMMENT ON COLUMN t_dept.id          IS '主键 ID';
COMMENT ON COLUMN t_dept.parent_id   IS '父部门 ID（0 表示顶级部门）';
COMMENT ON COLUMN t_dept.dept_name   IS '部门名称';
COMMENT ON COLUMN t_dept.sort        IS '排序号';
COMMENT ON COLUMN t_dept.status      IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN t_dept.create_time IS '创建时间';
COMMENT ON COLUMN t_dept.update_time IS '更新时间';
COMMENT ON COLUMN t_dept.deleted     IS '逻辑删除（0-未删除 1-已删除）';

-- -----------------------------------------------------------
-- 2. 用户表
-- -----------------------------------------------------------
CREATE TABLE t_user (
    id          BIGINT       NOT NULL,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64),
    email       VARCHAR(128),
    phone       VARCHAR(20),
    avatar      VARCHAR(512),
    dept_id     BIGINT,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL,
    update_time TIMESTAMP    NOT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (username)
);
CREATE INDEX idx_user_dept_id ON t_user (dept_id);
COMMENT ON TABLE  t_user IS '用户表';
COMMENT ON COLUMN t_user.id          IS '主键 ID';
COMMENT ON COLUMN t_user.username    IS '用户名（登录账号，唯一）';
COMMENT ON COLUMN t_user.password    IS '密码（BCrypt 加密存储）';
COMMENT ON COLUMN t_user.nickname    IS '昵称';
COMMENT ON COLUMN t_user.email       IS '邮箱';
COMMENT ON COLUMN t_user.phone       IS '手机号';
COMMENT ON COLUMN t_user.avatar      IS '头像 URL';
COMMENT ON COLUMN t_user.dept_id     IS '所属部门 ID';
COMMENT ON COLUMN t_user.status      IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN t_user.create_time IS '创建时间';
COMMENT ON COLUMN t_user.update_time IS '更新时间';
COMMENT ON COLUMN t_user.deleted     IS '逻辑删除（0-未删除 1-已删除）';

-- -----------------------------------------------------------
-- 3. 角色表（含数据权限范围）
-- -----------------------------------------------------------
CREATE TABLE t_role (
    id          BIGINT       NOT NULL,
    role_name   VARCHAR(64)  NOT NULL,
    role_code   VARCHAR(64)  NOT NULL,
    data_scope  SMALLINT     NOT NULL DEFAULT 4,
    sort        INT          NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    remark      VARCHAR(256),
    create_time TIMESTAMP    NOT NULL,
    update_time TIMESTAMP    NOT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (role_code)
);
COMMENT ON TABLE  t_role IS '角色表';
COMMENT ON COLUMN t_role.id          IS '主键 ID';
COMMENT ON COLUMN t_role.role_name   IS '角色名称';
COMMENT ON COLUMN t_role.role_code   IS '角色编码（唯一，如 ROLE_ADMIN）';
COMMENT ON COLUMN t_role.data_scope  IS '数据权限范围（1-全部 2-本部门及子部门 3-仅本部门 4-仅本人 5-自定义）';
COMMENT ON COLUMN t_role.sort        IS '排序号';
COMMENT ON COLUMN t_role.status      IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN t_role.remark      IS '备注';
COMMENT ON COLUMN t_role.create_time IS '创建时间';
COMMENT ON COLUMN t_role.update_time IS '更新时间';
COMMENT ON COLUMN t_role.deleted     IS '逻辑删除（0-未删除 1-已删除）';

-- -----------------------------------------------------------
-- 4. 功能权限表（菜单 / 按钮 / API 三级）
-- -----------------------------------------------------------
CREATE TABLE t_permission (
    id          BIGINT       NOT NULL,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    perm_name   VARCHAR(64)  NOT NULL,
    perm_code   VARCHAR(128) NOT NULL,
    perm_type   VARCHAR(16)  NOT NULL,
    path        VARCHAR(256),
    icon        VARCHAR(128),
    sort        INT          NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL,
    update_time TIMESTAMP    NOT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (perm_code)
);
CREATE INDEX idx_permission_parent_id ON t_permission (parent_id);
COMMENT ON TABLE  t_permission IS '功能权限表';
COMMENT ON COLUMN t_permission.id          IS '主键 ID';
COMMENT ON COLUMN t_permission.parent_id   IS '父权限 ID（0 表示顶级）';
COMMENT ON COLUMN t_permission.perm_name   IS '权限名称';
COMMENT ON COLUMN t_permission.perm_code   IS '权限编码（唯一，如 user:list、kb:read）';
COMMENT ON COLUMN t_permission.perm_type   IS '权限类型（MENU-菜单 / BUTTON-按钮 / API-接口）';
COMMENT ON COLUMN t_permission.path        IS '前端路由路径（仅菜单有效）';
COMMENT ON COLUMN t_permission.icon        IS '菜单图标（仅菜单有效）';
COMMENT ON COLUMN t_permission.sort        IS '排序号';
COMMENT ON COLUMN t_permission.status      IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN t_permission.create_time IS '创建时间';
COMMENT ON COLUMN t_permission.update_time IS '更新时间';
COMMENT ON COLUMN t_permission.deleted     IS '逻辑删除（0-未删除 1-已删除）';

-- -----------------------------------------------------------
-- 5. 用户-角色关联表
-- -----------------------------------------------------------
CREATE TABLE t_user_role (
    id          BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    deleted     SMALLINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (user_id, role_id)
);
CREATE INDEX idx_user_role_role_id ON t_user_role (role_id);
COMMENT ON TABLE  t_user_role IS '用户-角色关联表';
COMMENT ON COLUMN t_user_role.id          IS '主键 ID';
COMMENT ON COLUMN t_user_role.user_id     IS '用户 ID';
COMMENT ON COLUMN t_user_role.role_id     IS '角色 ID';
COMMENT ON COLUMN t_user_role.create_time IS '创建时间';
COMMENT ON COLUMN t_user_role.update_time IS '更新时间';
COMMENT ON COLUMN t_user_role.deleted     IS '逻辑删除（0-未删除 1-已删除）';

-- -----------------------------------------------------------
-- 6. 角色-权限关联表
-- -----------------------------------------------------------
CREATE TABLE t_role_permission (
    id            BIGINT    NOT NULL,
    role_id       BIGINT    NOT NULL,
    permission_id BIGINT    NOT NULL,
    create_time   TIMESTAMP NOT NULL,
    update_time   TIMESTAMP NOT NULL,
    deleted       SMALLINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (role_id, permission_id)
);
CREATE INDEX idx_role_permission_pid ON t_role_permission (permission_id);
COMMENT ON TABLE  t_role_permission IS '角色-权限关联表';
COMMENT ON COLUMN t_role_permission.id            IS '主键 ID';
COMMENT ON COLUMN t_role_permission.role_id       IS '角色 ID';
COMMENT ON COLUMN t_role_permission.permission_id IS '权限 ID';
COMMENT ON COLUMN t_role_permission.create_time   IS '创建时间';
COMMENT ON COLUMN t_role_permission.update_time   IS '更新时间';
COMMENT ON COLUMN t_role_permission.deleted        IS '逻辑删除（0-未删除 1-已删除）';

-- -----------------------------------------------------------
-- 7. 角色自定义数据范围表（data_scope=5 时生效）
-- -----------------------------------------------------------
CREATE TABLE t_role_data_scope (
    id          BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    dept_id     BIGINT    NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    deleted     SMALLINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (role_id, dept_id)
);
CREATE INDEX idx_role_data_scope_dept_id ON t_role_data_scope (dept_id);
COMMENT ON TABLE  t_role_data_scope IS '角色自定义数据范围表';
COMMENT ON COLUMN t_role_data_scope.id          IS '主键 ID';
COMMENT ON COLUMN t_role_data_scope.role_id     IS '角色 ID';
COMMENT ON COLUMN t_role_data_scope.dept_id     IS '可见部门 ID';
COMMENT ON COLUMN t_role_data_scope.create_time IS '创建时间';
COMMENT ON COLUMN t_role_data_scope.update_time IS '更新时间';
COMMENT ON COLUMN t_role_data_scope.deleted     IS '逻辑删除（0-未删除 1-已删除）';
