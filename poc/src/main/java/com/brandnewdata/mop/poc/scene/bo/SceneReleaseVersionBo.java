package com.brandnewdata.mop.poc.scene.bo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Getter
@Setter
public class SceneReleaseVersionBo {

    public static final Pattern PATTERN = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)_(\\d{8})");

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
