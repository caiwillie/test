package com.brandnewdata.mop.poc.scene.manager;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
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

    @Test
    void test2() {
        String encode = URLUtil.encode("【场景导出】", CharsetUtil.CHARSET_UTF_8);
        System.out.println(encode);
    }

}