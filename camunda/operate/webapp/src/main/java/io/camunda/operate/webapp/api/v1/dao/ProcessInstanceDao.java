/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.dao.PageableDao
 *  io.camunda.operate.webapp.api.v1.dao.SearchableDao
 *  io.camunda.operate.webapp.api.v1.dao.SortableDao
 *  io.camunda.operate.webapp.api.v1.entities.ChangeStatus
 *  io.camunda.operate.webapp.api.v1.entities.ProcessInstance
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 */
package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.dao.PageableDao;
import io.camunda.operate.webapp.api.v1.dao.SearchableDao;
import io.camunda.operate.webapp.api.v1.dao.SortableDao;
import io.camunda.operate.webapp.api.v1.entities.ChangeStatus;
import io.camunda.operate.webapp.api.v1.entities.ProcessInstance;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface ProcessInstanceDao
extends SearchableDao<ProcessInstance>,
SortableDao<ProcessInstance>,
PageableDao<ProcessInstance> {
    public ProcessInstance byKey(Long var1) throws APIException;

    public ChangeStatus delete(Long var1) throws APIException;
}
