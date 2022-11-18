package com.brandnewdata.mop.poc.operate.rest;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.GroupDeployDto;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto;
import com.brandnewdata.mop.poc.operate.resp.GroupDeployResp;
import com.brandnewdata.mop.poc.operate.service.GroupDeployService;
import com.brandnewdata.mop.poc.operate.service.ProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运行监控相关的接口
 *
 */
@RestController
public class GroupDeployRest {

    @Resource
    private IProcessDeployService deployService;

    @Autowired
    private GroupDeployService groupDeployService;

    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * 流程部署分页列表（deprecated）
     *
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @return result
     * @deprecated
     */
    @Deprecated
    @GetMapping("/rest/operate/deploy/page")
    public Result<Page<ProcessDeployDto>> page (
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ProcessDeployDto> page = deployService.page(pageNum, pageSize);
        return Result.OK(page);
    }

    /**
     * 流程部署分页列表（按照流程id分组）
     *
     * @param pageNum 分页页码
     * @param pageSize 分页大小
     * @return
     */
    @Deprecated
    @GetMapping("/rest/operate/deploy/groupPage")
    public Result<Page<GroupDeployResp>> groupPage (
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        // 先获取分页列表
        Page<GroupDeployDto> deployPage = groupDeployService.groupDeployPage(pageNum, pageSize);

        // 再获取 instance 信息

        // 转换成 resp

        List<GroupDeployResp> records = deployPage.getRecords().stream().map(this::toResp).collect(Collectors.toList());

        Page<GroupDeployResp> ret = new Page<>(deployPage.getTotal(), records);

        return Result.OK(ret);
    }

    private GroupDeployResp toResp(GroupDeployDto dto) {
        GroupDeployResp resp = new GroupDeployResp();
        resp.setProcessId(dto.getProcessId());
        resp.setProcessName(dto.getProcessName());

        // 设置部署版本数量和列表
        List<ProcessDeployDto> deploys = dto.getDeploys();
        int size = CollUtil.size(deploys);
        resp.setVersionCount(size);
        resp.setDeploys(deploys);
        if(size == 0) {
            return resp;
        }

        // 如果版本号不为0，需要查询运行实例
        List<Long> processDefinitionKeys = deploys.stream().map(ProcessDeployDto::getZeebeKey).collect(Collectors.toList());

        // 得到 flat map之后的 process instance list
        Map<Long, List<ListViewProcessInstanceDto>> map = processInstanceService.listByProcessDefinitionKeyList(processDefinitionKeys);
        List<ListViewProcessInstanceDto> collect = map.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        // 统计正常和异常的数量
        int activeCount = 0;
        int incidentCount = 0;
        for (ListViewProcessInstanceDto processInstanceDTO : collect) {
            if(processInstanceDTO.getState() == ProcessInstanceStateDto.INCIDENT) {
                incidentCount++;
            } else {
                activeCount++;
            }
        }

        resp.setIncidentInstanceCount(incidentCount);
        resp.setActiveInstanceCount(activeCount);

        return resp;
    }


    /**
     * 流程部署详情
     *
     * @param id 部署 id
     * @return the result
     */
    @GetMapping("/rest/operate/deploy/detail")
    @Deprecated
    public Result<ProcessDeployDto> detail(@RequestParam long id) {
        ProcessDeployDto deploy = deployService.getOne(id);
        Assert.notNull(deploy, "流程部署 {} 不存在", id);
        return Result.OK(deploy);
    }




}
