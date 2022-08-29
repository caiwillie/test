package com.brandnewdata.mop.poc.operate.rest;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDTO;
import com.brandnewdata.mop.poc.operate.resp.ProcessInstanceResp;
import com.brandnewdata.mop.poc.operate.service.ProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.ProcessInstanceDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运行监控相关的接口
 *
 */
@RestController
public class ProcessInstanceRest {

    @Resource
    private ProcessInstanceService instanceService;

    /**
     * 获取流程实例分页列表
     *
     * @param deployId 部署id
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @return result result
     */
    @GetMapping("/rest/operate/instance/page")
    public Result<Page<ProcessInstanceResp>> page (
            @RequestParam Long deployId,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ListViewProcessInstanceDTO> page = instanceService.page(deployId, pageNum, pageSize);


        List<ListViewProcessInstanceDTO> records = page.getRecords();
        List<ProcessInstanceResp> respList = records.stream().map(dto -> {
            ProcessInstanceResp resp = new ProcessInstanceResp();
            return resp.from(dto);
        }).collect(Collectors.toList());

        return Result.OK(new Page<>(page.getTotal(), respList));
    }

    /**
     * 获取流程实例详情
     *
     * @param processInstanceId 流程实例id
     * @return the result
     */
    @GetMapping("/rest/operate/instance/detail")
    public Result<ProcessInstanceDTO> detail(@RequestParam String processInstanceId) {
        return null;
    }



}
