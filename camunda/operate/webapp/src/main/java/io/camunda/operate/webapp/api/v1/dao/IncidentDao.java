package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.entities.Incident;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface IncidentDao extends SearchableDao, SortableDao, PageableDao {
   Incident byKey(Long var1) throws APIException;
}
