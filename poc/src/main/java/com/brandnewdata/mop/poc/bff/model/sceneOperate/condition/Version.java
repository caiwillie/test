package com.brandnewdata.mop.poc.bff.model.sceneOperate.condition;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Version {

    /**
     * 版本
     */
    private int version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
