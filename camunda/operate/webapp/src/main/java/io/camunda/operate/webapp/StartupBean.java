/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.data.DataGenerator
 *  io.camunda.operate.es.ElasticsearchConnector
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.webapp.security.es.ElasticSearchUserDetailsService
 *  io.camunda.operate.webapp.zeebe.operation.OperationExecutor
 *  javax.annotation.PostConstruct
 *  javax.annotation.PreDestroy
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.DependsOn
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp;

import io.camunda.operate.data.DataGenerator;
import io.camunda.operate.es.ElasticsearchConnector;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.webapp.security.es.ElasticSearchUserDetailsService;
import io.camunda.operate.webapp.zeebe.operation.OperationExecutor;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@DependsOn(value={"schemaStartup"})
@Profile(value={"!test"})
public class StartupBean {
    private static final Logger logger = LoggerFactory.getLogger(StartupBean.class);
    @Autowired
    private RestHighLevelClient esClient;
    @Autowired
    private RestHighLevelClient zeebeEsClient;
    @Autowired(required=false)
    private ElasticSearchUserDetailsService elasticsearchUserDetailsService;
    @Autowired
    private DataGenerator dataGenerator;
    @Autowired
    private OperationExecutor operationExecutor;
    @Autowired
    private OperateProperties operateProperties;

    @PostConstruct
    public void initApplication() {
        if (this.elasticsearchUserDetailsService != null) {
            logger.info("INIT: Create users in elasticsearch if not exists ...");
            this.elasticsearchUserDetailsService.initializeUsers();
        }
        logger.debug("INIT: Generate demo data...");
        try {
            this.dataGenerator.createZeebeDataAsync(false);
        }
        catch (Exception ex) {
            logger.debug("Demo data could not be generated. Cause: {}", (Object)ex.getMessage());
            logger.error("Error occurred when generating demo data.", ex);
        }
        logger.info("INIT: Start operation executor...");
        this.operationExecutor.startExecuting();
        logger.info("INIT: DONE");
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutdown elasticsearch clients.");
        ElasticsearchConnector.closeEsClient((RestHighLevelClient)this.esClient);
        ElasticsearchConnector.closeEsClient((RestHighLevelClient)this.zeebeEsClient);
    }
}
