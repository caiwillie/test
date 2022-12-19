package com.brandnewdata.mop.poc.lock;

import cn.hutool.core.util.IdUtil;
import com.caiwillie.util.lock.DatabaseDistributedLock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class LockTest {
    @Resource
    private DataSource dataSource;

    @Test
    void test1() {
        DatabaseDistributedLock<String, Boolean> distributedLock =
                new DatabaseDistributedLock<>(dataSource, "mop_lock", "resource_digest",
                        "lock_status", "lock_version", "update_time", 10000L,
                        Boolean::booleanValue, () -> Boolean.TRUE, () -> Boolean.FALSE);
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("id", IdUtil.getSnowflakeNextId());
        fieldMap.put("create_time", new Date());
        fieldMap.put("resource_content", "hello world");

        Long haha = distributedLock.lock("haha", () -> fieldMap);
        return;
    }

    @Test
    void test2() {
        DatabaseDistributedLock<String, Boolean> distributedLock =
                new DatabaseDistributedLock<>(dataSource, "mop_lock", "resource_digest",
                        "lock_status", "lock_version", "update_time", 10000L,
                        Boolean::booleanValue, () -> Boolean.TRUE, () -> Boolean.FALSE);

        distributedLock.unlock("haha", 2L);
    }
}
