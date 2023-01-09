package com.brandnewdata.mop.poc.bff.vo.scene;

import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class SceneVo {
    /**
     * 场景 id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 场景名称
     */
    private String name;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 图片URL
     */
    private String imgUrl;

    /**
     * 流程个数
     */
    private Integer processCount;

    public SceneVo from(SceneDto dto) {
        this.setId(dto.getId());
        this.setCreateTime(dto.getCreateTime());
        this.setUpdateTime(dto.getUpdateTime());
        this.setName(dto.getName());
        return this;
    }

}
