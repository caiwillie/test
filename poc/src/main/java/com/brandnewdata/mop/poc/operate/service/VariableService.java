package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.lang.Assert;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.brandnewdata.mop.poc.operate.dto.VariableDto;
import com.brandnewdata.mop.poc.operate.schema.template.VariableTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VariableService {

    public List<VariableDto> listByScopeId(String processInstanceId, String scopeId) {
        Assert.notNull(processInstanceId);
        Assert.notNull(scopeId);
        /*new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder().term(t -> t.field(VariableTemplate.))))*/
        return null;
    }

}
