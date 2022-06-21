package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.entities.Query;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public interface SortableDao {
   void buildSorting(Query var1, String var2, SearchSourceBuilder var3);
}
