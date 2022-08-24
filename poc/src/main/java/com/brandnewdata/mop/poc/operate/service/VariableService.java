package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.brandnewdata.mop.poc.operate.dao.VariableDao;
import com.brandnewdata.mop.poc.operate.dto.VariableDto;
import com.brandnewdata.mop.poc.operate.entity.ProcessEntity;
import com.brandnewdata.mop.poc.operate.entity.VariableEntity;
import com.brandnewdata.mop.poc.operate.schema.template.VariableTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VariableService {

    @Autowired
    private VariableDao variableDao;

    public List<VariableDto> listByScopeId(String processInstanceId, String scopeId) {
        List<VariableDto> ret = new ArrayList<>();

        Assert.notNull(processInstanceId);
        Assert.notNull(scopeId);
        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(VariableTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                                .build(), new Query.Builder()
                                .term(t -> t.field(VariableTemplate.SCOPE_KEY).value(scopeId))
                                .build())
                        .build())
                .build();

        List<VariableEntity> entities = variableDao.list(query);

        for (VariableEntity entity : entities) {
            ret.add(toDto(entity));
        }

        if(CollUtil.isNotEmpty(ret)) {
            // 给第一个加上 first = true
            ret.get(0).setFirst(true);
        }

        return ret;
    }


    private VariableDto toDto(VariableEntity entity) {
        VariableDto dto = new VariableDto();
        dto.fromEntity(entity);
        return dto;
    }

}
