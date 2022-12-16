package com.brandnewdata.mop.poc.bff.vo.homepage;

import lombok.Data;

/**
 * 运行中场景(列表内实体)
 *
 * @author jekyll 2022-12-14 10:42
 */
@Data
public class SceneListBriefVo {

    private String id;

    /**
     * *场景版本描述
     * */
    private String versionDesc;
    /**
     * *场景状态
     * */
    private String state;

    /**
     * 7日运行总数描述
     * * */
    private String totalRunDesc;

    /**
     * 7日失败总数描述
     * */
    private Integer totalFailDesc;

}
