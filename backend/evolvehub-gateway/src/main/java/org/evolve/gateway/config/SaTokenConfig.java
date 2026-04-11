package org.evolve.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token gateway-level authentication filter (Reactor / WebFlux).
 */
@Configuration
public class SaTokenConfig {

    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                // Intercept all routes
                .addInclude("/**")
                // Exclude public endpoints (login, health check, etc.)
                .addExclude("/api/auth/login", "/actuator/**")
                // Auth logic
                .setAuth(obj -> {
                    SaRouter.match("/**", StpUtil::checkLogin);
                })
                // Exception handler: return JSON
                .setError(e -> String.format(
                        "{\"code\":401,\"msg\":\"%s\",\"data\":null}", e.getMessage()));
    }
}
