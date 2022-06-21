package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.entities.ChangeStatus;
import io.camunda.operate.webapp.api.v1.entities.ProcessInstance;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface ProcessInstanceDao extends SearchableDao, SortableDao, PageableDao {
   ProcessInstance byKey(Long var1) throws APIException;

   ChangeStatus delete(Long var1) throws APIException;
}
