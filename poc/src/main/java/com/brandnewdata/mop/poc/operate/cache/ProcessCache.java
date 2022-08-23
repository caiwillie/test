package com.brandnewdata.mop.poc.operate.cache;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.thread.ThreadUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.brandnewdata.mop.poc.operate.dao.ProcessDao;
import com.brandnewdata.mop.poc.operate.entity.ProcessEntity;
import com.brandnewdata.mop.poc.operate.schema.index.ProcessIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class ProcessCache {

    private static final int CACHE_MAX_SIZE = 1000;
    private static final int MAX_ATTEMPTS = 5;
    private static final long WAIT_TIME = 200L;

    private static final LFUCache<Long, ProcessEntity> cache = CacheUtil.newLFUCache(CACHE_MAX_SIZE);


    @Autowired
    private ProcessDao processDao;

    public ProcessEntity getOne(Long processDefinitionKey) {
        return cache.get(processDefinitionKey, findOrWaitProcess(processDefinitionKey));
    }

    private Func0<ProcessEntity> findOrWaitProcess(Long processDefinitionKey) {
        return () -> {
            int attemptsCount = 0;
            Optional<ProcessEntity> foundProcess = Optional.empty();

            while(!foundProcess.isPresent() && attemptsCount < MAX_ATTEMPTS) {
                ++attemptsCount;
                foundProcess = this.readProcessByKey(processDefinitionKey);
                if (!foundProcess.isPresent()) {
                    log.debug("Unable to find process {}. {} attempts left. Waiting {} ms.", processDefinitionKey, MAX_ATTEMPTS - attemptsCount, WAIT_TIME);
                    ThreadUtil.sleep(WAIT_TIME);
                } else {
                    log.debug("Found process {} after {} attempts. Waited {} ms.", processDefinitionKey, attemptsCount, (long)(attemptsCount - 1) * WAIT_TIME);
                }
            }

            return foundProcess.orElse(null);
        };
    }

    private Optional<ProcessEntity> readProcessByKey(Long processDefinitionKey) {
        Query query = new Query.Builder()
                .term(new TermQuery.Builder().field(ProcessIndex.KEY).value(processDefinitionKey).build())
                .build();
        ProcessEntity processEntity = processDao.getOne(query);
        return Optional.ofNullable(processEntity);
    }



}
