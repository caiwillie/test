/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.es.contract.MetricContract$Reader
 *  io.camunda.operate.es.dao.Query
 *  io.camunda.operate.es.dao.UsageMetricDAO
 *  io.camunda.operate.es.dao.response.AggregationResponse
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.es.contract.MetricContract;
import io.camunda.operate.es.dao.Query;
import io.camunda.operate.es.dao.UsageMetricDAO;
import io.camunda.operate.es.dao.response.AggregationResponse;
import io.camunda.operate.exceptions.OperateRuntimeException;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricReader
implements MetricContract.Reader {
    public static final String PROCESS_INSTANCES_AGG_NAME = "process_instances";
    public static final String DECISION_INSTANCES_AGG_NAME = "decision_instances";
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricReader.class);
    @Autowired
    private UsageMetricDAO dao;

    public Long retrieveProcessInstanceCount(OffsetDateTime startTime, OffsetDateTime endTime) {
        int limit = 1;
        Query query = Query.whereEquals((String)"event", (String)"EVENT_PROCESS_INSTANCE_FINISHED").and(Query.range((String)"eventTime", (Object)startTime, (Object)endTime)).aggregate(PROCESS_INSTANCES_AGG_NAME, "value", limit);
        AggregationResponse response = this.dao.searchWithAggregation(query);
        if (!response.hasError()) return response.getSumOfTotalDocs();
        String message = "Error while retrieving process instance count between dates";
        LOGGER.error("Error while retrieving process instance count between dates");
        throw new OperateRuntimeException("Error while retrieving process instance count between dates");
    }

    public Long retrieveDecisionInstanceCount(OffsetDateTime startTime, OffsetDateTime endTime) {
        int limit = 1;
        Query query = Query.whereEquals((String)"event", (String)"EVENT_DECISION_INSTANCE_EVALUATED").and(Query.range((String)"eventTime", (Object)startTime, (Object)endTime)).aggregate(DECISION_INSTANCES_AGG_NAME, "value", limit);
        AggregationResponse response = this.dao.searchWithAggregation(query);
        if (!response.hasError()) return response.getSumOfTotalDocs();
        String message = "Error while retrieving decision instance count between dates";
        LOGGER.error("Error while retrieving decision instance count between dates");
        throw new OperateRuntimeException("Error while retrieving decision instance count between dates");
    }
}