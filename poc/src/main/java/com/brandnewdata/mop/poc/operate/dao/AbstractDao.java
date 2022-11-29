package com.brandnewdata.mop.poc.operate.dao;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDao {

    // @Autowired
    protected ElasticsearchClient client;

}
