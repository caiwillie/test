package com.brandnewdata.mop.poc.proxy.cache;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.db.Entity;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyEndpointCallPo;
import com.caiwillie.util.cache.ScheduleUpdateCache;
import com.google.common.cache.Cache;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class ProxyEndpointCallCache {

    private ScheduleUpdateCache<Long, ProxyEndpointCallDto> scheduleCache;

    public Map<Long, ProxyEndpointCallDto> asMap() {
        return Collections.unmodifiableMap(scheduleCache.asMap());
    }

    public ProxyEndpointCallCache(DataSource dataSource) {
        scheduleCache = new ScheduleUpdateCache<>("mop_proxy_endpoint_call", "id", "update_time", dataSource,
                "0/4 * * * * ?", 10, getConsume());
    }

    private BiConsumer<List<Entity>, Cache<Long, ProxyEndpointCallDto>> getConsume() {
        return (entities, cache) -> {
            for (Entity entity : entities) {
                ProxyEndpointCallDto dto = toDto(entity);
                cache.put(dto.getId(), dto);
            }
        };
    }

    private ProxyEndpointCallDto toDto(Entity entity) {
        ProxyEndpointCallDto dto = new ProxyEndpointCallDto();
        dto.setId(entity.getLong(ProxyEndpointCallPo.ID));
        dto.setStartTime(LocalDateTimeUtil.of(entity.getDate(ProxyEndpointCallPo.START_TIME)));
        dto.setEndpointId(entity.getLong(ProxyEndpointCallPo.ENDPOINT_ID));
        dto.setExecuteStatus(entity.getStr(ProxyEndpointCallPo.EXECUTE_STATUS));
        dto.setTimeConsuming(entity.getInt(ProxyEndpointCallPo.TIME_CONSUMING));
        return dto;
    }
}
