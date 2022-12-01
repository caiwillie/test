package com.brandnewdata.mop.poc.bff.vo.scene;

import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class SceneVersionVo {
    /**
     * 版本id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 版本名称
     */
    private String version;

    /**
     * 场景id
     */
    private Long sceneId;

    /**
     * 状态。1 配置中；2 运行中；3 已停止；4 调试中
     */
    private Integer status;

    /**
     * 关联的环境列表
     */
    private List<EnvVo> envList;

    /**
     * 描述
     */
    private String description;
}
