package com.brandnewdata.mop.poc.jackson;

import cn.hutool.core.map.MapUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import org.junit.jupiter.api.Test;

public class JacksonTest {

    @Test
    void test1() {
        String str = JacksonUtil.to(MapUtil.empty());
        return;
    }
}
