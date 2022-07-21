package com.brandnewdata.mop.poc.service;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brandnewdata.common.constant.TriggerProtocolConstant;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.connector.api.IConnectorCommonTriggerProcessConfFeign;
import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.mop.poc.common.Constants;
import com.brandnewdata.mop.poc.common.service.result.PageResult;
import com.brandnewdata.mop.poc.dao.DeModelDao;
import com.brandnewdata.mop.poc.parser.XMLDTO;
import com.brandnewdata.mop.poc.parser.XMLParseStep1;
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

    @Resource
    private IConnectorConfFeign confClient;

    private static final ObjectMapper OM = new ObjectMapper();


    public void save(DeModelEntity entity) {
        XMLDTO xmlDTO = new XMLParser3()
                .parse(entity.getEditorXml())
                .replaceCustomTrigger().build();

        // 通过 modelKey 查询 id
        Long id = Optional.ofNullable(getOne(xmlDTO.getModelKey()))
                .map(DeModelEntity::getId).orElse(null);
        entity.setName(xmlDTO.getName());
        entity.setModelKey(xmlDTO.getModelKey());
        if(id == null) {
            modelDao.insert(entity);
        } else {
            entity.setId(id);
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

    public void deploy(String modelKey, String name, String editorXMl, int triggerType) {


        XMLDTO xmldto = null;

        XMLParseStep1.XMLParseStep2 step2 = new XMLParser3(modelKey, name)
                .parse(editorXMl);
        if(triggerType == Constants.TRIGGER_TYPE_NONE) {
            xmldto = step2.replaceProperties(confClient).build();
        } else if (triggerType == Constants.TRIGGER_TYPE_GENERAL) {
            xmldto = step2.replaceGeneralTrigger().replaceProperties(confClient).build();
        } else if (triggerType == Constants.TRIGGER_TYPE_CUSTOM) {
            xmldto = step2.replaceCustomTrigger().replaceProperties(confClient).build();
        } else {
            throw new IllegalArgumentException("触发器类型不支持");
        }

        IConnectorCommonTriggerProcessConfFeign.ConnectorCommonTriggerProcessConfParamDTO triggerProcessConfig =
                new IConnectorCommonTriggerProcessConfFeign.ConnectorCommonTriggerProcessConfParamDTO();

        zeebe.newDeployResourceCommand()
                .addResourceStringUtf8(xmldto.getZeebeXML(), xmldto.getModelKey() + ".bpmn")
                .send()
                .join();

        if(triggerType == Constants.TRIGGER_TYPE_CUSTOM) {
            Integer success = null;
            try {
                triggerProcessConfig.setProcessId(xmldto.getModelKey());
                triggerProcessConfig.setProcessName(xmldto.getName());
                triggerProcessConfig.setProtocol(TriggerProtocolConstant.SCHEDULED);
                triggerProcessConfig.setConfig(OM.writeValueAsString(xmldto.getRequestParamConfigs()));
                triggerProcessConfig.setTriggerFullId(xmldto.getTriggerFullId());
                success = triggerProcessConfClient.save(triggerProcessConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if(success == null || success != 1) {
                throw new IllegalArgumentException("shenme");
            }
        }

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
