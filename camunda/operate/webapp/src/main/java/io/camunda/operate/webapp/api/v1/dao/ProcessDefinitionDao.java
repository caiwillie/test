/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.dao.PageableDao
 *  io.camunda.operate.webapp.api.v1.dao.SearchableDao
 *  io.camunda.operate.webapp.api.v1.dao.SortableDao
 *  io.camunda.operate.webapp.api.v1.entities.ProcessDefinition
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 */
package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.dao.PageableDao;
import io.camunda.operate.webapp.api.v1.dao.SearchableDao;
import io.camunda.operate.webapp.api.v1.dao.SortableDao;
import io.camunda.operate.webapp.api.v1.entities.ProcessDefinition;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface ProcessDefinitionDao
extends SearchableDao<ProcessDefinition>,
SortableDao<ProcessDefinition>,
PageableDao<ProcessDefinition> {
    public ProcessDefinition byKey(Long var1) throws APIException;

    public String xmlByKey(Long var1) throws APIException;
}
