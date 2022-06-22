/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 */
package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.entities.Query;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public interface PageableDao<T> {
    public void buildPaging(Query<T> var1, SearchSourceBuilder var2);
}
