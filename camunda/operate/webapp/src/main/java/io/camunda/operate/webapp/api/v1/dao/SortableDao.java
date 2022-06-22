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

public interface SortableDao<T> {
    public void buildSorting(Query<T> var1, String var2, SearchSourceBuilder var3);
}
