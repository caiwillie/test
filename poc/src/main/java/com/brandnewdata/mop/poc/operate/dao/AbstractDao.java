package com.brandnewdata.mop.poc.operate.dao;


import co.elastic.clients.elasticsearch.ElasticsearchClient;

public abstract class AbstractDao {

    // @Autowired
    protected ElasticsearchClient client;

}
