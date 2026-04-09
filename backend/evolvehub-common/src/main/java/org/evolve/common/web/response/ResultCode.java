package org.evolve.common.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务状态码枚举
 * <p>
 * 编码规则：
 * - 200：成功
 * - 400~499：客户端错误（参数、认证、权限等）
 * - 500~599：服务端错误
 * - 1xxx：业务模块自定义错误
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ========== 通用 ==========
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // ========== 客户端错误 4xx ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    // ========== 服务端错误 5xx ==========
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // ========== 认证 & 权限 1xxx ==========
    TOKEN_EXPIRED(1001, "Token已过期"),
    TOKEN_INVALID(1002, "Token无效"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    PASSWORD_ERROR(1004, "密码错误"),

    // ========== 数据 2xxx ==========
    DATA_NOT_EXIST(2001, "数据不存在"),
    DATA_ALREADY_EXIST(2002, "数据已存在"),
    DATA_SCOPE_DENIED(2003, "数据权限不足"),
    ;

    private final int code;
    private final String message;
}
