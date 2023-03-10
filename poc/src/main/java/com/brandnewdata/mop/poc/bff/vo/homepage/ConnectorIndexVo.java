package com.brandnewdata.mop.poc.bff.vo.homepage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最近更新连接器(列表内实体)
 *
 * @author jekyll 2022-12-14 14:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorIndexVo {

    private String id;

    private String name;

    private String version;

    private String icon;

    private String firm;

    private Integer connectorType;

    /**
     * 连接器类别 1:应用 2:数据 3: 组件 4:设备 5:算法
     */
    private Integer connectorCategory;

}
