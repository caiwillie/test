package com.brandnewdata.mop.poc.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

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
        ret.setBasePackage(String.join(",",
                "com.brandnewdata.mop.poc.dao",
                "com.brandnewdata.mop.poc.scene.dao",
                "com.brandnewdata.mop.poc.process.dao",
                "com.brandnewdata.mop.poc.proxy.dao"));
        return ret;
    }

    @Bean("coreMainSqlSessionFactoryBean")
    public static SqlSessionFactory sqlSessionFactoryBean(
            DataSource dataSource,
            ResourcePatternResolver resourcePatResolver,
            @Value("${brandnewdata.datasource.schema}") String schema) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        // factory.setConfiguration(null); ??????mybatis????????????
        factory.setGlobalConfig(globalConfig(schema)); // mybatis plus?????????????????????
        factory.setPlugins(mybatisPlusInterceptor()); // mybatis plus??????????????????
        // factory.setMapperLocations(resourcePatResolver.getResources(LOCATION_PATTERN));
        SqlSessionFactory sqlSessionFactory = factory.getObject();
        return sqlSessionFactory;
    }

    private static GlobalConfig globalConfig(String schema) {
        GlobalConfig config = new GlobalConfig();

        // ????????????????????????
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setSchema(schema);
        config.setDbConfig(dbConfig);

        // ???????????????
        config.setMetaObjectHandler(new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                Date now = DateUtil.date().toJdkDate();
                this.strictInsertFill(metaObject, "createTime", Date.class, now);
                this.strictInsertFill(metaObject, "updateTime", Date.class, now);
            }
            @Override
            public void updateFill(MetaObject metaObject) {
                Date now = DateUtil.date().toJdkDate();
                this.strictUpdateFill(metaObject, "updateTime", Date.class, now);
            }

            @Override
            public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
                if (metaObject.getValue(fieldName) == null
                        || StrUtil.equals(fieldName, "updateTime")) {
                    // ???????????? updateTime ???????????????????????????????????????
                    Object obj = fieldVal.get();
                    if (Objects.nonNull(obj)) {
                        metaObject.setValue(fieldName, obj);
                    }
                }
                return this;
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
