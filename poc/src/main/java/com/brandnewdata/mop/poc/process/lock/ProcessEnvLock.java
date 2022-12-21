package com.brandnewdata.mop.poc.process.lock;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.caiwillie.util.lock.DatabaseDistributedLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProcessEnvLock {

    private static final String RESOURCE_CONTENT_TEMPLATE = "processId_{};env_{}";

    @Value("${brandnewdata.distribute-lock.expiration-time.env-process}")
    private long EXPIRATION;

    private final DatabaseDistributedLock<String, String> distributedLock;

    public ProcessEnvLock(DataSource dataSource) {
        distributedLock = new DatabaseDistributedLock<>(dataSource, "mop_lock", "resource_digest",
                "lock_status", "lock_version", "update_time", EXPIRATION,
                status -> StrUtil.equals(status, StringPool.TRUE), () -> StringPool.TRUE, () -> StringPool.FALSE);
    }

    public Long lock(String processId, Long envId) {
        String resourceContent = StrUtil.format(RESOURCE_CONTENT_TEMPLATE, processId, envId);
        String resourceDigest = DigestUtil.md5Hex(resourceContent);

        Map<String, Object> addSupplier = new HashMap<>();
        addSupplier.put("id", IdUtil.getSnowflakeNextId());
        addSupplier.put("create_time", new Date());
        addSupplier.put("resource_content", resourceContent);
        return distributedLock.lock(resourceDigest, () -> addSupplier);
    }

    public void unlock(String processId, Long envId, Long preVersion) {
        String resourceContent = StrUtil.format(RESOURCE_CONTENT_TEMPLATE, processId, envId);
        String resourceDigest = DigestUtil.md5Hex(resourceContent);

        distributedLock.unlock(resourceDigest, preVersion);
    }
}
