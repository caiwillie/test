package com.brandnewdata.mop.poc.scene.manager;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Component
public class JooqManager {

    @Resource
    private DataSource dataSource;

    public DSLContext create() {
        return DSL.using(dataSource, SQLDialect.MYSQL);
    }

}
