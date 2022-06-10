package com.brandnewdata.mop.poc.config;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author caiwillie
 */
@Configuration("modelMybatisPlusConfiguration")
public class MybatisPlusConfiguration {
    // private static final String LOCATION_PATTERN = "classpath*:com/shinemo/suc/core/mappers/DataDao.xml";

    @Bean("modelMainMapperScannerConfigurer")
    public static MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer ret = new MapperScannerConfigurer();
        ret.setSqlSessionFactoryBeanName("coreMainSqlSessionFactoryBean");
        ret.setBasePackage("com.brandnewdata.mop.poc.dao");
        return ret;
    }

    @Bean("coreMainSqlSessionFactoryBean")
    public static SqlSessionFactory sqlSessionFactoryBean(
            DataSource dataSource,
            ResourcePatternResolver resourcePatResolver,
            @Value("${brandnewdata.datasource.schema}") String schema) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        // factory.setConfiguration(null); 原生mybatis相关配置
        factory.setGlobalConfig(globalConfig(schema)); // mybatis plus新增的相关配置
        // factory.setPlugins(mybatisPlusInterceptor()); // mybatis plus插件相关配置
        // factory.setMapperLocations(resourcePatResolver.getResources(LOCATION_PATTERN));
        return factory.getObject();
    }

    private static GlobalConfig globalConfig(String schema) {
        GlobalConfig config = new GlobalConfig();

        // 数据库相关的配置
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setSchema(schema);
        config.setDbConfig(dbConfig);

        // 钩子处理器
        config.setMetaObjectHandler(new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

        });
        return config;
    }

    private static MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor ret = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor =
                new PaginationInnerInterceptor();
        OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor =
                new OptimisticLockerInnerInterceptor();
        ret.setInterceptors(Arrays.asList(
                paginationInnerInterceptor,
                optimisticLockerInnerInterceptor));
        return ret;
    }


}
