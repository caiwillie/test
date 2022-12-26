package com.brandnewdata.mop.poc.scene.bo.export;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SceneExportFileBo {

    List<ProcessExportBo> processExportBoList;

    List<ConnectorExportBo> connectorExportBoList;
}
