package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.brandnewdata.mop.poc.operate.converter.VariableDtoConverter;
import com.brandnewdata.mop.poc.operate.dao.VariableDao;
import com.brandnewdata.mop.poc.operate.dto.VariableDto;
import com.brandnewdata.mop.poc.operate.entity.VariableEntity;
import com.brandnewdata.mop.poc.operate.manager.DaoManager;
import com.brandnewdata.mop.poc.operate.schema.template.VariableTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VariableService2 implements IVariableService2 {

    private final DaoManager daoManager;

    public VariableService2(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    @Override
    public List<VariableDto> listByScopeId(Long envId, String processInstanceId, String scopeId) {
        Assert.notNull(processInstanceId, "流程实例id不能为空");
        Assert.notNull(scopeId, "scopeId不能为空");
        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(VariableTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                                .build(), new Query.Builder()
                                .term(t -> t.field(VariableTemplate.SCOPE_KEY).value(scopeId))
                                .build())
                        .build())
                .build();

        log.debug(query.toString());

        VariableDao variableDao = daoManager.getVariableDaoByEnvId(envId);
        List<VariableEntity> entities = variableDao.list(query);

        List<VariableDto> variableDtoList = entities.stream().map(VariableDtoConverter::createFrom).collect(Collectors.toList());

        if(CollUtil.isNotEmpty(variableDtoList)) {
            // 给第一个加上 first = true
            variableDtoList.get(0).setFirst(true);
        }

        return variableDtoList;
    }

}
