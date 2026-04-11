package org.evolve.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 后台管理访问控制 Filter
 * <p>
 * 拦截 /admin/** 路径，只允许 SUPER_ADMIN 和 ADMIN 角色访问
 * 其他角色访问返回 403 权限不足
 * </p>
 *
 * @author zhao
 */
@Component
public class AdminAccessFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 检查是否是后台管理路径
        if (path.startsWith("/admin") || path.startsWith("/api/admin")) {
            try {
                // 检查是否登录
                if (!StpUtil.isLogin()) {
                    return unauthorized(exchange, "未登录，请先登录");
                }

                // 获取用户角色
                Object loginId = StpUtil.getLoginIdDefaultNull();
                if (loginId == null) {
                    return unauthorized(exchange, "未登录，请先登录");
                }

                // 检查角色
                boolean hasAdminRole = false;
                try {
                    hasAdminRole = StpUtil.hasRole("SUPER_ADMIN") || StpUtil.hasRole("ADMIN");
                } catch (Exception e) {
                    // 角色查询失败，拒绝访问
                    return forbidden(exchange, "权限不足，请联系管理员");
                }

                if (!hasAdminRole) {
                    return forbidden(exchange, "权限不足，请联系管理员");
                }

            } catch (Exception e) {
                return unauthorized(exchange, "认证失败，请重新登录");
            }
        }

        // 放行
        return chain.filter(exchange);
    }

    /**
     * 返回 401 未授权
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json; charset=utf-8");
        String body = String.format("{\"code\":401,\"msg\":\"%s\",\"data\":null}", message);
        org.springframework.http.HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * 返回 403 权限不足
     */
    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json; charset=utf-8");
        String body = String.format("{\"code\":403,\"msg\":\"%s\",\"data\":null}", message);
        org.springframework.http.HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在 SaReactorFilter 之后执行
        return -100;
    }
}
