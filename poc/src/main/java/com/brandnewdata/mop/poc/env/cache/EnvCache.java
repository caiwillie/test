package com.brandnewdata.mop.poc.env.cache;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.po.EnvPo;
import com.caiwillie.util.cache.ScheduleUpdateCache;
import com.google.common.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class EnvCache {

    private final ScheduleUpdateCache<Long, EnvDto> scheduleCache;

    public EnvCache(DataSource dataSource,
                    @Value("${brandnewdata.database-schedule.maxRowSize}") int maxRowSize) {
        scheduleCache = new ScheduleUpdateCache<>("mop_env", "id", "update_time", dataSource,
                "0/4 * * * * ?", maxRowSize, getConsume());

    }

    public Map<Long, EnvDto> asMap() {
        return scheduleCache.asMap();
    }

    private BiConsumer<List<Entity>, Cache<Long, EnvDto>> getConsume() {
        return (entities, cache) -> {
            for (Entity entity : entities) {
                EnvDto envDto = toDto(entity);
                Long id = envDto.getId();
                if(StrUtil.equals(envDto.getStatus(), "Active")) {
                    cache.put(id, envDto);
                } else {
                    cache.invalidate(id);
                }
            }
        };
    }

    private EnvDto toDto(Entity entity) {
        EnvDto envDto = new EnvDto();
        envDto.setId(entity.getLong(EnvPo.ID));
        envDto.setCreateTime(LocalDateTimeUtil.of(entity.getDate(EnvPo.CREATE_TIME)));
        envDto.setUpdateTime(LocalDateTimeUtil.of(entity.getDate(EnvPo.UPDATE_TIME)));
        envDto.setDeployTime(Opt.ofNullable(entity.getDate(EnvPo.DEPLOY_TIME))
                .map(LocalDateTimeUtil::of).orElse(null));
        envDto.setName(entity.getStr(EnvPo.NAME));
        envDto.setNamespace(entity.getStr(EnvPo.NAMESPACE));
        envDto.setStatus(entity.getStr(EnvPo.STATUS));
        envDto.setType(entity.getInt(EnvPo.TYPE));
        envDto.setDescription(entity.getStr(EnvPo.DESCRIPTION));
        return envDto;
    }
}
