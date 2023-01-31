package com.brandnewdata.mop.poc.proxy.cache;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
import com.caiwillie.util.cache.ScheduleUpdateCache;
import com.google.common.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class ProxyCache {

    private final String domainPattern;

    private ScheduleUpdateCache<Long, ProxyDto> scheduleCache;

    public ProxyCache(DataSource dataSource,
                      @Value("${brandnewdata.api.domainPattern}") String domainPattern,
                      @Value("${brandnewdata.database-schedule.maxRowSize}") int maxRowSize) {
        this.domainPattern = domainPattern;
        scheduleCache = new ScheduleUpdateCache<>("mop_reverse_proxy", "id", "update_time", dataSource,
                "0/4 * * * * ?", maxRowSize, getConsume());
    }

    public Map<Long, ProxyDto> asMap() {
        return Collections.unmodifiableMap(scheduleCache.asMap());
    }

    private BiConsumer<List<Entity>, Cache<Long, ProxyDto>> getConsume() {
        return (entities, cache) -> {
            for (Entity entity : entities) {
                Long id = entity.getLong(ProxyPo.ID);
                if(entity.getLong(ProxyPo.DELETE_FLAG) != null) {
                    cache.invalidate(id);
                } else {
                    ProxyDto dto = toDto(entity);
                    cache.put(id, dto);
                }
            }
        };
    }

    private ProxyDto toDto(Entity entity) {
        ProxyDto dto = new ProxyDto();
        dto.setId(entity.getLong(ProxyPo.ID));
        dto.setCreateTime(LocalDateTimeUtil.of(entity.getDate(ProxyPo.CREATE_TIME)));
        dto.setUpdateTime(LocalDateTimeUtil.of(entity.getDate(ProxyPo.UPDATE_TIME)));
        dto.setName(entity.getStr(ProxyPo.NAME));
        dto.setProtocol(entity.getInt(ProxyPo.PROTOCOL));
        dto.setVersion(entity.getStr(ProxyPo.VERSION));
        dto.setDomain(StrUtil.format(domainPattern, entity.getStr(ProxyPo.DOMAIN)));
        dto.setDescription(entity.getStr(ProxyPo.DESCRIPTION));
        dto.setTag(entity.getStr(ProxyPo.TAG));
        dto.setState(entity.getInt(ProxyPo.STATE));
        dto.setProjectId(entity.getLong(ProxyPo.PROJECT_ID));
        return dto;
    }

}
