package org.evolve.common.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.evolve.common.web.response.Result;
import org.evolve.common.web.response.ResultCode;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 鉴权异常处理器
 * <p>
 * 拦截 Sa-Token 抛出的 NotLoginException / NotPermissionException / NotRoleException，
 * 返回标准 JSON 响应。优先级高于全局兜底处理器。
 * 放置在 common 模块中，各业务模块自动生效。
 * </p>
 *
 * @author zhao
 */
@Slf4j
@Order(-1)
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 未登录 / Token 无效
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLogin(NotLoginException e, HttpServletRequest request) {
        log.warn("[鉴权失败] {} - 未登录: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 缺少权限
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermission(NotPermissionException e, HttpServletRequest request) {
        log.warn("[鉴权失败] {} - 缺少权限: {}", request.getRequestURI(), e.getPermission());
        return Result.fail(ResultCode.FORBIDDEN, "缺少权限: " + e.getPermission());
    }

    /**
     * 缺少角色
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotRole(NotRoleException e, HttpServletRequest request) {
        log.warn("[鉴权失败] {} - 缺少角色: {}", request.getRequestURI(), e.getRole());
        return Result.fail(ResultCode.FORBIDDEN, "缺少角色: " + e.getRole());
    }
}
