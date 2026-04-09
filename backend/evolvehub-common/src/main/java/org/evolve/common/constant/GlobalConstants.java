package org.evolve.common.constant;

/**
 * 全局常量
 */
public final class GlobalConstants {

    private GlobalConstants() {}

    // ==================== 系统 ====================

    /** 超级管理员角色编码 */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = "123456";

    /** 顶级节点父 ID（部门、权限树根节点） */
    public static final Long TOP_PARENT_ID = 0L;

    // ==================== 状态 ====================

    /** 正常 */
    public static final int STATUS_ENABLE = 1;

    /** 禁用 */
    public static final int STATUS_DISABLE = 0;

    // ==================== 逻辑删除 ====================

    /** 未删除 */
    public static final int NOT_DELETED = 0;

    /** 已删除 */
    public static final int DELETED = 1;

    // ==================== 数据权限范围 ====================

    /** 全部数据 */
    public static final int DATA_SCOPE_ALL = 1;

    /** 本部门及子部门 */
    public static final int DATA_SCOPE_DEPT_AND_CHILD = 2;

    /** 仅本部门 */
    public static final int DATA_SCOPE_DEPT = 3;

    /** 仅本人 */
    public static final int DATA_SCOPE_SELF = 4;

    /** 自定义 */
    public static final int DATA_SCOPE_CUSTOM = 5;

    // ==================== 权限类型 ====================

    /** 菜单 */
    public static final String PERM_TYPE_MENU = "MENU";

    /** 按钮 */
    public static final String PERM_TYPE_BUTTON = "BUTTON";

    /** 接口 */
    public static final String PERM_TYPE_API = "API";

    // ==================== 知识库可见范围 ====================

    /** 全局可见 */
    public static final String KB_SCOPE_GLOBAL = "GLOBAL";

    /** 部门可见 */
    public static final String KB_SCOPE_DEPT = "DEPT";

    /** 项目可见 */
    public static final String KB_SCOPE_PROJECT = "PROJECT";

    /** 敏感（需显式授权） */
    public static final String KB_SCOPE_SENSITIVE = "SENSITIVE";

    // ==================== 分页 ====================

    /** 默认页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大每页条数 */
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== Token ====================

    /** Token Header 名称 */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
}
