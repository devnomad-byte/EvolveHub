package org.evolve.auth.config;

import jakarta.annotation.Resource;
import org.evolve.auth.user.service.DataScopeWebInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 注册数据权限拦截器。
 * Sa-Token 拦截器已由 evolvehub-common 统一注册，此处仅注册 auth 模块特有的拦截器。
 * </p>
 *
 * @author zhao
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private DataScopeWebInterceptor dataScopeWebInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 数据权限拦截器：设置当前用户的数据权限上下文
        registry.addInterceptor(dataScopeWebInterceptor)
                .addPathPatterns("/**");
    }
}
