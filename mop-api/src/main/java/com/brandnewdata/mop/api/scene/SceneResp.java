package com.brandnewdata.mop.api.scene;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SceneResp {

    /**
     * 场景 id
     */
    private Long id;

    /**
     * 场景名称
     */
    private String name;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 图片URL
     */
    private String imgUrl;
}
