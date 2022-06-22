/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.dao.PageableDao
 *  io.camunda.operate.webapp.api.v1.dao.SearchableDao
 *  io.camunda.operate.webapp.api.v1.dao.SortableDao
 *  io.camunda.operate.webapp.api.v1.entities.FlowNodeInstance
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 */
package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.dao.PageableDao;
import io.camunda.operate.webapp.api.v1.dao.SearchableDao;
import io.camunda.operate.webapp.api.v1.dao.SortableDao;
import io.camunda.operate.webapp.api.v1.entities.FlowNodeInstance;
import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public interface FlowNodeInstanceDao
extends SearchableDao<FlowNodeInstance>,
SortableDao<FlowNodeInstance>,
PageableDao<FlowNodeInstance> {
    public FlowNodeInstance byKey(Long var1) throws APIException;
}
