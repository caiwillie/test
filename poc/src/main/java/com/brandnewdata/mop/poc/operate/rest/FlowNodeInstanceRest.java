package com.brandnewdata.mop.poc.operate.rest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlowNodeInstanceRest {

    @Autowired
    private ElasticsearchClient client;

    @PostMapping("/rest/operate/flowNodeInstance/list")
    public void list() {



    }
}
