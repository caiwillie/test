package com.brandnewdata.mop.poc.env.cache;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.db.Entity;
import com.brandnewdata.mop.poc.env.dto.EnvServiceDto;
import com.brandnewdata.mop.poc.env.po.EnvServicePo;
import com.caiwillie.util.cache.ScheduleUpdateCache;
import com.google.common.cache.Cache;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class EnvServiceCache {

    private final ScheduleUpdateCache<Long, EnvServiceDto> scheduleCache;

    public EnvServiceCache(DataSource dataSource) {
        scheduleCache = new ScheduleUpdateCache<>("mop_env_service", "id", "update_time", dataSource,
                "0/4 * * * * ?", 10, getConsume());
    }

    private BiConsumer<List<Entity>, Cache<Long, EnvServiceDto>> getConsume() {
        return (entities, cache) -> {
            for (Entity entity : entities) {
                EnvServiceDto envDto = toDto(entity);
                Long id = envDto.getId();
                cache.put(id, envDto);
            }
        };
    }

    private EnvServiceDto toDto(Entity entity) {
        EnvServiceDto ret = new EnvServiceDto();
        ret.setId(entity.getLong(EnvServicePo.ID));
        ret.setCreateTime(LocalDateTimeUtil.of(entity.getDate(EnvServicePo.CREATE_TIME)));
        ret.setUpdateTime(LocalDateTimeUtil.of(entity.getDate(EnvServicePo.UPDATE_TIME)));
        ret.setName(entity.getStr(EnvServicePo.NAME));
        ret.setEnvId(entity.getLong(EnvServicePo.ENV_ID));
        ret.setClusterIp(entity.getStr(EnvServicePo.CLUSTER_IP));
        ret.setPorts(entity.getStr(EnvServicePo.PORTS));
        return ret;
    }

    public Map<Long, EnvServiceDto> asMap() {
        return scheduleCache.asMap();
    }
}
