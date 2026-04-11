package org.evolve.aiplatform.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * 多数据源配置
 * <p>
 * - 主数据源（ai）：evolve_ai 库，用于 AI 模块的知识库、文档、切片等表
 * - 从数据源（auth）：evolve_auth 库，用于读取公共表（部门、用户等）
 * </p>
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Configuration
public class DataSourceConfig {

    // ==================== 主数据源（AI 库）====================

    @Primary
    @Bean(name = "aiDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.ai")
    public DataSource aiDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "aiSqlSessionFactory")
    public SqlSessionFactory aiSqlSessionFactory(
            @Qualifier("aiDataSource") DataSource dataSource,
            MetaObjectHandler metaObjectHandler) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml")
        );

        // 配置 MyBatis-Plus
        com.baomidou.mybatisplus.core.MybatisConfiguration configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);

        // 注入自动填充处理器
        com.baomidou.mybatisplus.core.config.GlobalConfig globalConfig = new com.baomidou.mybatisplus.core.config.GlobalConfig();
        globalConfig.setMetaObjectHandler(metaObjectHandler);
        factoryBean.setGlobalConfig(globalConfig);

        return factoryBean.getObject();
    }

    @Primary
    @Bean(name = "aiSqlSessionTemplate")
    public SqlSessionTemplate aiSqlSessionTemplate(@Qualifier("aiSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    // ==================== 从数据源（Auth 库，读取公共表）====================

    @Bean(name = "authDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.auth")
    public DataSource authDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "authSqlSessionFactory")
    public SqlSessionFactory authSqlSessionFactory(
            @Qualifier("authDataSource") DataSource dataSource,
            MetaObjectHandler metaObjectHandler) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // 配置 MyBatis-Plus
        MybatisConfiguration configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);

        // 注入自动填充处理器
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(metaObjectHandler);
        factoryBean.setGlobalConfig(globalConfig);

        return factoryBean.getObject();
    }

    @Bean(name = "authSqlSessionTemplate")
    public SqlSessionTemplate authSqlSessionTemplate(@Qualifier("authSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * AI 模块 Mapper 扫描（使用主数据源）
     */
    @Configuration
    @MapperScan(
            basePackages = "org.evolve.aiplatform.mapper",
            sqlSessionTemplateRef = "aiSqlSessionTemplate"
    )
    static class AiMapperScanConfig {
    }

    /**
     * 公共模块 Mapper 扫描（使用 Auth 数据源）
     * 包含：部门、用户、角色权限等公共表的查询
     */
    @Configuration
    @MapperScan(
            basePackages = {
                    "org.evolve.common.infra",
                    "org.evolve.common.config"
            },
            sqlSessionTemplateRef = "authSqlSessionTemplate"
    )
    static class CommonMapperScanConfig {
    }
}