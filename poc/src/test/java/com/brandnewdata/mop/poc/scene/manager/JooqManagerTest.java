package com.brandnewdata.mop.poc.scene.manager;

import com.baomidou.mybatisplus.annotation.TableName;
import com.brandnewdata.mop.poc.scene.po.ScenePo;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

class JooqManagerTest {

    @Resource
    private JooqManager jooqManager;

    @Test
    void test1() {
        TableName table = ScenePo.class.getAnnotation(TableName.class);
        String value = table.value();
        return;
    }

}