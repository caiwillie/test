package com.brandnewdata.mop.poc.operate.cache;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.thread.ThreadUtil;
import com.brandnewdata.mop.poc.operate.entity.ProcessEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class ProcessCache {

    private static final int CACHE_MAX_SIZE = 1000;
    private static final int MAX_ATTEMPTS = 5;
    private static final long WAIT_TIME = 200L;

    private static final LFUCache<Long, ProcessEntity> cache = CacheUtil.newLFUCache(CACHE_MAX_SIZE);


    public ProcessEntity getOne(Long processDefinitionKey) {
        cache.get(processDefinitionKey, findOrWaitProcess(processDefinitionKey));
    }

    private Func0<ProcessEntity> findOrWaitProcess(Long processDefinitionKey) {
        return () -> {
            int attemptsCount = 0;
            Optional<ProcessEntity> foundProcess = Optional.empty();

            while(!foundProcess.isPresent() && attemptsCount < MAX_ATTEMPTS) {
                ++attemptsCount;
                foundProcess = this.readProcessByKey(processDefinitionKey);
                if (!foundProcess.isPresent()) {
                    log.debug("Unable to find process {}. {} attempts left. Waiting {} ms.", new Object[]{processDefinitionKey, attempts - attemptsCount, sleepInMilliseconds});
                    ThreadUtil.sleep(WAIT_TIME);
                } else {
                    log.debug("Found process {} after {} attempts. Waited {} ms.", new Object[]{processDefinitionKey, attemptsCount, (long)(attemptsCount - 1) * sleepInMilliseconds});
                }
            }

            return foundProcess;
        };
    }

    private Optional<ProcessEntity> readProcessByKey(Long processDefinitionKey) {

    }



}
