package com.brandnewdata.mop.poc.env.lock;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.caiwillie.util.lock.DatabaseDistributedLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EnvLock {

    private static final String RESOURCE_CONTENT_TEMPLATE = "env_{}";

    private final DatabaseDistributedLock<String, String> distributedLock;

    public EnvLock(DataSource dataSource,
                   @Value("${brandnewdata.distribute-lock.expiration-time.env}") long expiration) {
        distributedLock = new DatabaseDistributedLock<>(dataSource, "mop_lock", "resource_digest",
                "lock_status", "lock_version", "update_time", expiration,
                status -> StrUtil.equals(status, StringPool.TRUE), () -> StringPool.TRUE, () -> StringPool.FALSE);
    }

    public Long lock(Long envId) {
        String resourceContent = StrUtil.format(RESOURCE_CONTENT_TEMPLATE, envId);
        String resourceDigest = DigestUtil.md5Hex(resourceContent);

        Map<String, Object> addSupplier = new HashMap<>();
        addSupplier.put("id", IdUtil.getSnowflakeNextId());
        addSupplier.put("create_time", new Date());
        addSupplier.put("resource_content", resourceContent);
        return distributedLock.lock(resourceDigest, () -> addSupplier);
    }

    public void unlock(Long envId, Long preVersion) {
        String resourceContent = StrUtil.format(RESOURCE_CONTENT_TEMPLATE, envId);
        String resourceDigest = DigestUtil.md5Hex(resourceContent);

        distributedLock.unlock(resourceDigest, preVersion);
    }


}
