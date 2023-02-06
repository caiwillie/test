package datasource;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointCallDao;
import lombok.SneakyThrows;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

public class MybatisPlusMapperUtil {

    private static final DataSource dataSource;

    static {
        dataSource = new SimpleDataSource(DataSourceEnum.POC.getJdbcUrl(),
                DataSourceEnum.POC.getUser(), DataSourceEnum.POC.getPassword());
    }

    @SneakyThrows
    public static <T> T get(Class<T> clazz) {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(clazz);
        factory.setConfiguration(configuration);
        factory.setDataSource(dataSource);
        factory.setGlobalConfig(globalConfig("mop")); // mybatis plus新增的相关配置
        factory.setPlugins(mybatisPlusInterceptor()); // mybatis plus插件相关配置
        try {
            return factory.getObject().openSession().getMapper(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                    // 避免因为 updateTime 中存在旧值，就不更新的状况
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

    public static void main(String[] args) {
        ProxyEndpointCallDao proxyEndpointCallDao = MybatisPlusMapperUtil.get(ProxyEndpointCallDao.class);
        Long count = proxyEndpointCallDao.selectCount(new QueryWrapper<>());
        return;
    }
}
