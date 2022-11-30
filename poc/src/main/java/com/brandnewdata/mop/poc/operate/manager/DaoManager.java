package com.brandnewdata.mop.poc.operate.manager;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.brandnewdata.mop.poc.operate.dao.*;
import org.springframework.stereotype.Component;

@Component
public class DaoManager {

    private final ElasticsearchManager elasticsearchManager;

    public DaoManager(ElasticsearchManager elasticsearchManager) {
        this.elasticsearchManager = elasticsearchManager;
    }


    /**
     * 根据envId获取ListViewDao
     *
     * @param envId
     * @return
     */
    public ListViewDao getListViewDaoByEnvId(Long envId) {
        ElasticsearchClient elasticsearchClient = elasticsearchManager.getByEnvId(envId);
        return ListViewDao.getInstance(elasticsearchClient);
    }

    /**
     * 根据envId获取SequenceFlowDao
     *
     * @param envId
     * @return
     */
    public SequenceFlowDao getSequenceFlowDaoByEnvId(Long envId) {
        ElasticsearchClient elasticsearchClient = elasticsearchManager.getByEnvId(envId);
        return SequenceFlowDao.getInstance(elasticsearchClient);
    }


    /**
     * 根据envId获取FlowNodeInstanceDao
     *
     * @param envId
     * @return
     */
    public FlowNodeInstanceDao getFlowNodeInstanceDaoByEnvId(Long envId) {
        ElasticsearchClient elasticsearchClient = elasticsearchManager.getByEnvId(envId);
        return FlowNodeInstanceDao.getInstance(elasticsearchClient);
    }

    /**
     * 根据envId获取EventDao
     *
     * @param envId
     * @return
     */
    public EventDao getEventDaoByEnvId(Long envId) {
        ElasticsearchClient elasticsearchClient = elasticsearchManager.getByEnvId(envId);
        return EventDao.getInstance(elasticsearchClient);
    }

    /**
     * 根据envId获取IncidentDao
     *
     * @param envId
     * @return
     */
    public IncidentDao getIncidentDaoByEnvId(Long envId) {
        ElasticsearchClient elasticsearchClient = elasticsearchManager.getByEnvId(envId);
        return IncidentDao.getInstance(elasticsearchClient);
    }

    /**
     * 根据envId获取variableDao
     *
     * @param envId the env id
     * @return the variable dao by env id
     */
    public VariableDao getVariableDaoByEnvId(Long envId) {
        ElasticsearchClient elasticsearchClient = elasticsearchManager.getByEnvId(envId);
        return VariableDao.getInstance(elasticsearchClient);
    }

}
