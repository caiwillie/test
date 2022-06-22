/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.dao.PageableDao
 *  io.camunda.operate.webapp.api.v1.dao.SearchableDao
 *  io.camunda.operate.webapp.api.v1.dao.SortableDao
 *  io.camunda.operate.webapp.api.v1.entities.Incident
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 */
package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.dao.PageableDao;
import io.camunda.operate.webapp.api.v1.dao.SearchableDao;
import io.camunda.operate.webapp.api.v1.dao.SortableDao;
import io.camunda.operate.webapp.api.v1.entities.Incident;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface IncidentDao
extends SearchableDao<Incident>,
SortableDao<Incident>,
PageableDao<Incident> {
    public Incident byKey(Long var1) throws APIException;
}
