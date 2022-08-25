package com.brandnewdata.mop.poc.operate.rest;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.GroupDeployDTO;
import com.brandnewdata.mop.poc.operate.resp.GroupDeployResp;
import com.brandnewdata.mop.poc.operate.service.GroupDeployService;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GroupDeployRest {

    @Resource
    private IProcessDeployService deployService;

    @Autowired
    private GroupDeployService groupDeployService;

    /**
     * 流程部署分页列表（deprecated）
     *
     * @param pageNum  分页页码
     * @param pageSize 分页大小
     * @return result
     * @deprecated
     */
    @GetMapping("/rest/operate/deploy/page")
    public Result<Page<ProcessDeploy>> page (
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        Page<ProcessDeploy> page = deployService.page(pageNum, pageSize);
        return Result.OK(page);
    }

    /**
     * 流程部署分页列表（按照流程id分组）
     *
     * @param pageNum 分页页码
     * @param pageSize 分页大小
     * @return
     */
    @GetMapping("/rest/operate/deploy/groupPage")
    public Result<Page<GroupDeployResp>> groupPage (
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        // 先获取分页列表
        Page<GroupDeployDTO> deployPage = groupDeployService.groupDeployPage(pageNum, pageSize);

        // 再获取 instance 信息


        // 转换成 resp

        List<GroupDeployResp> records = deployPage.getRecords().stream().map(this::toResp).collect(Collectors.toList());

        Page<GroupDeployResp> ret = new Page<>(deployPage.getTotal(), records);

        return Result.OK(rest);
    }

    private GroupDeployResp toResp(GroupDeployDTO dto) {
        GroupDeployResp resp = new GroupDeployResp();
        resp.setProcessId(dto.getProcessId());
        resp.setProcessName(dto.getProcessName());

        // 设置部署版本数量和列表
        List<ProcessDeploy> deploys = dto.getDeploys();
        resp.setVersionCount(CollUtil.size(deploys));
        resp.setDeploys(deploys);
        return resp;
    }


    /**
     * 流程部署详情
     *
     * @param id 部署 id
     * @return the result
     */
    @GetMapping("/rest/operate/deploy/detail")
    public Result<ProcessDeploy> detail(@RequestParam long id) {
        ProcessDeploy deploy = deployService.getOne(id);
        Assert.notNull(deploy, "流程部署 {} 不存在", id);
        return Result.OK(deploy);
    }




}
