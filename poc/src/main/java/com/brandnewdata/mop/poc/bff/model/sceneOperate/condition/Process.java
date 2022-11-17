package com.brandnewdata.mop.poc.bff.model.sceneOperate.condition;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Process {

    private String processId;

    private String name;

    private List<Integer> versionList;

}
