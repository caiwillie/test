package com.brandnewdata.mop.poc.scene.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.scene.po.ScenePo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SceneDto2 {
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
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


    public SceneDto2 from(ScenePo po) {
        this.setId(po.getId());
        this.setName(po.getName());
        this.setCreateTime(LocalDateTimeUtil.of(po.getCreateTime()));
        this.setUpdateTime(LocalDateTimeUtil.of(po.getUpdateTime()));
        return this;
    }
}
