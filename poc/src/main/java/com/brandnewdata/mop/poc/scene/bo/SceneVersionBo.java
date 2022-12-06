package com.brandnewdata.mop.poc.scene.bo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SceneVersionBo {

    /**
     * 主版本号
     */
    int major;

    /**
     * 副版本
     */
    int minor;

    /**
     * 补丁版本
     */
    int patch;

    /**
     * 日期
     */
    LocalDate date;
}
