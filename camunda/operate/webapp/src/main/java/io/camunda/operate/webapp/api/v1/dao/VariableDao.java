package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.entities.Variable;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface VariableDao extends SearchableDao, SortableDao, PageableDao {
   Variable byKey(Long var1) throws APIException;
}
