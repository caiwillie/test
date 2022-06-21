package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.entities.FlowNodeInstance;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface FlowNodeInstanceDao extends SearchableDao, SortableDao, PageableDao {
   FlowNodeInstance byKey(Long var1) throws APIException;
}
