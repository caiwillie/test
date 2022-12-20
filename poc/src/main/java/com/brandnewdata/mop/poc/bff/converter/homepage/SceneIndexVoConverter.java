package com.brandnewdata.mop.poc.bff.converter.homepage;

import com.brandnewdata.mop.api.bff.home.dto.HomeSceneDto;
import com.brandnewdata.mop.poc.bff.vo.homepage.SceneListBriefVo;

/**
 * @author jekyll 2022-12-20 16:48
 */
public class SceneIndexVoConverter {

    public static SceneListBriefVo createForm(HomeSceneDto dto){
        SceneListBriefVo vo = new SceneListBriefVo();
        vo.setState(dto.getStatus());
        vo.setTotalFailDesc(dto.getProcessInstanceFailCount()+"");
        vo.setTotalRunDesc(dto.getProcessInstanceCount()+"");
        vo.setUpdateTime(dto.getUpdateTime());
        vo.setVersionDesc(dto.getVersion());
        vo.setName(dto.getName());
        vo.setEnv(dto.getEnv());
        return vo;
    }
}
