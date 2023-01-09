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
     * 状态。1 配置中；2 运行中；3 已停止；4 调试中, 5 调试部署中，6 发布部署中
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

    /**
     * 0 发布中；1 发布完成；2 发布异常
     */
    private Integer deployStatus;

    /**
     * 部署进度（当deployStatus = 0时用）
     */
    private Double deployProgressPercentage;

    /**
     * 异常信息（当deployStatus = 2时用）
     */
    private String exceptionMessage;
}
