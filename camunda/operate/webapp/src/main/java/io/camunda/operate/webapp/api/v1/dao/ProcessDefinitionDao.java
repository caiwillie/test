package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.entities.ProcessDefinition;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface ProcessDefinitionDao extends SearchableDao, SortableDao, PageableDao {
   ProcessDefinition byKey(Long var1) throws APIException;

   String xmlByKey(Long var1) throws APIException;
}
