package com.brandnewdata.mop.poc.service;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brandnewdata.connector.api.IConnectorCommonTriggerProcessConfFeign;
import com.brandnewdata.mop.poc.common.service.result.PageResult;
import com.brandnewdata.mop.poc.dao.DeModelDao;
import com.brandnewdata.mop.poc.message.MessageDTO;
import com.brandnewdata.mop.poc.parser.XMLDTO;
import com.brandnewdata.mop.poc.parser.XMLParser3;
import com.brandnewdata.mop.poc.pojo.entity.DeModelEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * @author caiwillie
 */
@Service
public class ModelService {

    @Resource
    private DeModelDao modelDao;

    @Resource
    private ZeebeClient zeebe;

    @Resource
    private IConnectorCommonTriggerProcessConfFeign triggerProcessConfClient;

    private static final ObjectMapper OM = new ObjectMapper();


    public void save(DeModelEntity entity) {
        Long id = entity.getId();
        XMLDTO xmlDTO = new XMLParser3()
                .parse(entity.getEditorXml())
                .replaceCustomTrigger().build();
        entity.setName(xmlDTO.getName());
        entity.setModelKey(xmlDTO.getModelKey());
        if(id == null) {
            modelDao.insert(entity);
        } else {
            modelDao.updateById(entity);
        }
        return;
    }

    public DeModelEntity getOne(String modelKey) {
        Assert.notNull(modelKey, "模型标识不能为空");
        QueryWrapper<DeModelEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DeModelEntity.MODEL_KEY, modelKey);
        return modelDao.selectOne(queryWrapper);
    }

    public PageResult<DeModelEntity> page(Integer pageNumber, Integer pageSize) {
        PageResult<DeModelEntity> ret = new PageResult<>();

        Assert.notNull(pageNumber, "pageNumber不能为空");
        Assert.isTrue(pageNumber > 0, "pageNumber需要大于零");
        Assert.notNull(pageSize, "pageSize不能为空");
        Assert.isTrue(pageSize > 0, "pageSize需要大于零");

        Page<DeModelEntity> page = new Page<>(pageNumber, pageSize);
        page = modelDao.selectPage(page, new QueryWrapper<>());

        ret.setTotal(page.getTotal());
        ret.setRecords(page.getRecords());
        return ret;
    }

    public void deploy(String modelKey, String name, String editorXMl) {
        XMLDTO xmldto = new XMLParser3(modelKey, name)
                .parse(editorXMl).replaceCustomTrigger().build();

        IConnectorCommonTriggerProcessConfFeign.ConnectorCommonTriggerProcessConfParamDTO triggerProcessConfig =
                new IConnectorCommonTriggerProcessConfFeign.ConnectorCommonTriggerProcessConfParamDTO();

        zeebe.newDeployResourceCommand()
                .addResourceStringUtf8(xmldto.getZeebeXML(), xmldto.getModelKey() + ".bpmn")
                .send()
                .join();

        try {
            triggerProcessConfig.setProcessId(xmldto.getModelKey());
            triggerProcessConfig.setProtocol("HTTP");
            triggerProcessConfig.setConfig(OM.writeValueAsString(xmldto.getRequestParamConfigs()));
            triggerProcessConfig.setTriggerFullId(xmldto.getTriggerFullId());
            triggerProcessConfClient.save(triggerProcessConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return;
    }

    public void start(String processId, Map<String, Object> values) {

        zeebe.newCreateInstanceCommand()
                .bpmnProcessId(ServiceUtil.convertModelKey(processId))
                .latestVersion()
                .variables(values)
                .send()
                .join();
    }

    public Map<String, Object> startWithResult(String processId, Map<String, Object> values) {
        ProcessInstanceResult result = zeebe.newCreateInstanceCommand()
                .bpmnProcessId(ServiceUtil.convertModelKey(processId))
                .latestVersion()
                .variables(values)
                .withResult()
                .send()
                .join();

        return result.getVariablesAsMap();
    }

    public void sendMessage(String message, Map<String, Object> values) {
        zeebe.newPublishMessageCommand()
                .messageName(message)
                .correlationKey(StrUtil.EMPTY)
                .variables(values)
                .send()
                .join();
    }

}
