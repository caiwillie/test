package com.brandnewdata.mop.poc.bff.model.sceneOperate.condition;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Version {

    private int version;

    private LocalDateTime createTime;
}
