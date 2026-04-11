package org.evolve.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 全局配置
 * <p>
 * 放置在 common 模块中，通过 Spring 组件扫描自动生效，避免各业务模块重复配置。
 * 包含：
 * <ul>
 *     <li>SaInterceptor 注册 —— 启用 @SaCheckRole / @SaCheckPermission 等注解鉴权</li>
 *     <li>StpInterface 实现 —— 从数据库查询角色码和权限码</li>
 * </ul>
 * </p>
 *
 * @author zhao
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register");
    }

    // ==================== StpInterface 实现 ====================

    /**
     * Sa-Token 权限/角色数据提供者（通用版）
     * <p>
     * 通过 SQL 直接 JOIN 查询，不依赖任何业务模块的 Infra 类。
     * 所有连接同一数据库的模块自动生效。
     * </p>
     */
    @Component
    public static class StpInterfaceComponent implements StpInterface {

        @Resource
        private SaTokenRoleMapper saTokenRoleMapper;

        @Override
        public List<String> getPermissionList(Object loginId, String loginType) {
            long userId = Long.parseLong(loginId.toString());
            List<String> perms = saTokenRoleMapper.selectPermCodesByUserId(userId);
            return perms != null ? perms : Collections.emptyList();
        }

        @Override
        public List<String> getRoleList(Object loginId, String loginType) {
            long userId = Long.parseLong(loginId.toString());
            List<String> roles = saTokenRoleMapper.selectRoleCodesByUserId(userId);
            return roles != null ? roles : Collections.emptyList();
        }
    }

    @Mapper
    interface SaTokenRoleMapper {

        /**
         * 查询用户的角色码列表
         * 查询链路: eh_user_role → eh_role.role_code
         */
        @Select("""
            SELECT DISTINCT r.role_code
            FROM eh_user_role ur
            JOIN eh_role r ON ur.role_id = r.id AND r.deleted = 0 AND r.status = 1
            WHERE ur.user_id = #{userId} AND ur.deleted = 0
        """)
        List<String> selectRoleCodesByUserId(long userId);

        /**
         * 查询用户的权限码列表
         * 查询链路: eh_user_role → eh_role_permission → eh_permission.perm_code
         */
        @Select("""
            SELECT DISTINCT p.perm_code
            FROM eh_user_role ur
            JOIN eh_role_permission rp ON ur.role_id = rp.role_id AND rp.deleted = 0
            JOIN eh_permission p ON rp.permission_id = p.id AND p.deleted = 0 AND p.status = 1
            WHERE ur.user_id = #{userId} AND ur.deleted = 0
        """)
        List<String> selectPermCodesByUserId(long userId);
    }
}
