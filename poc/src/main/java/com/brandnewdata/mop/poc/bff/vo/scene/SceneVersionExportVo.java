package com.brandnewdata.mop.poc.bff.vo.scene;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SceneVersionExportVo {

    private String version;

    private List<String> processIds;
}
