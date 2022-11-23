package com.brandnewdata.mop.poc.bff.vo.scene.external;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ExportQuery {

    /**
     * 版本id
     */
    private Long versionId;

    /**
     * 流程列表
     */
    private List<String> processIdList;
}
