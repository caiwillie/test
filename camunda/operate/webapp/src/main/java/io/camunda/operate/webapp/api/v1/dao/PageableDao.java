package io.camunda.operate.webapp.api.v1.dao;

import io.camunda.operate.webapp.api.v1.entities.Query;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public interface PageableDao {
   void buildPaging(Query var1, SearchSourceBuilder var2);
}
