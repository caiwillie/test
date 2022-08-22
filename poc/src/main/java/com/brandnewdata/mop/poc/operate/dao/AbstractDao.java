package com.brandnewdata.mop.poc.operate.dao;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.brandnewdata.mop.poc.operate.schema.index.IndexDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDao {

    @Autowired
    protected ElasticsearchClient client;

}
