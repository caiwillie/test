package com.brandnewdata.mop.poc.service;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brandnewdata.mop.poc.common.service.result.PageResult;
import com.brandnewdata.mop.poc.dao.DeModelDao;
import com.brandnewdata.mop.poc.parser.XMLDTO;
import com.brandnewdata.mop.poc.parser.XMLParser2;
import com.brandnewdata.mop.poc.pojo.entity.DeModelEntity;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author caiwillie
 */
@Service
public class ModelService {

    @Resource
    private DeModelDao modelDao;

    @Resource
    private ZeebeClient zeebe;


    public void save(DeModelEntity entity) {
        Long id = entity.getId();
        XMLDTO xmlDTO = new XMLParser2().parse(entity.getEditorXml());
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
        modelKey = ServiceUtil.convertModelKey(modelKey);
        XMLDTO xmldto = new XMLParser2(modelKey, name).parse(editorXMl);

        DeploymentEvent join = zeebe.newDeployResourceCommand()
                .addResourceStringUtf8(xmldto.getZeebeXML(), modelKey + ".bpmn")
                .send()
                .join();
    }

    public void start(String modelKey) {
        // camunda-cloud-quick-start-advanced
        modelKey = ServiceUtil.convertModelKey(modelKey);



        zeebe.newCreateInstanceCommand()
                .bpmnProcessId(modelKey)
                .latestVersion()
                .send()
                .join();
    }

}
