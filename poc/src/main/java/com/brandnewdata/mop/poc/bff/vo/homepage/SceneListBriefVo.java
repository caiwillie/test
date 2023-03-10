package com.brandnewdata.mop.poc.bff.vo.homepage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运行中场景(列表内实体)
 *
 * @author jekyll 2022-12-14 10:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SceneListBriefVo {

    private String id;

    /**
     * *场景版本描述
     * */
    private String versionDesc;

    /**
     * *场景状态
     * */
    private Integer state;


    private String updateTime;

    /**
     * 7日运行总数描述
     * * */
    private String totalRunDesc;

    /**
     * 7日失败总数描述
     * */
    private String totalFailDesc;

    private String name;

    private String env;

}
