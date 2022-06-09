package com.brandnewdata.mop.modeler.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author caiwillie
 */
@Configuration
public class DatasourceConfiguration {

    @Bean(initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource(
            @Value("${brandnewdata.datasource.driver-class-name}") String driverClassName,
            @Value("${brandnewdata.datasource.url}") String url,
            @Value("${brandnewdata.datasource.username}") String username,
            @Value("${brandnewdata.datasource.password}") String password) {
        DruidDataSource ret = new DruidDataSource();
        ret.setDriverClassName(driverClassName);
        ret.setUrl(url);
        ret.setUsername(username);
        ret.setPassword(password);
        ret.setInitialSize(2);
        ret.setMinIdle(2);
        ret.setMaxActive(10);
        ret.setProxyFilters(Arrays.asList(statFilter()));
        return ret;
    }

    private StatFilter statFilter() {
        StatFilter ret = new StatFilter();
        ret.setSlowSqlMillis(1000);
        ret.setLogSlowSql(true);
        return ret;
    }

}
