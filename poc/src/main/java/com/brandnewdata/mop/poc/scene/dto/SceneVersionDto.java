package com.brandnewdata.mop.poc.scene.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SceneVersionDto {
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String version;

    private Long sceneId;

    private Integer status;

    public SceneVersionDto from(SceneVersionPo po) {
        this.setId(po.getId());
        this.setCreateTime(LocalDateTimeUtil.of(po.getCreateTime()));
        this.setUpdateTime(LocalDateTimeUtil.of(po.getUpdateTime()));
        this.setVersion(po.getVersion());
        this.setSceneId(po.getSceneId());
        this.setStatus(po.getStatus());
        return this;
    }
}
