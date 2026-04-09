package org.evolve.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 全局配置
 * <p>
 * 配置 MyBatis-Plus 的全局拦截器，包括分页插件等。
 * 放置在 common 模块中，通过 Spring 组件扫描自动生效，避免各业务模块重复配置。
 * </p>
 *
 * @author zhao
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器
     * <p>当前配置了分页插件 {@link PaginationInnerInterceptor}，
     * 用于支持 MyBatis-Plus 的分页查询功能。</p>
     *
     * @return MyBatis-Plus 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
