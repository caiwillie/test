package com.brandnewdata.mop.poc.scene.converter;

import com.brandnewdata.mop.poc.scene.bo.export.ConfigExportBo;
import com.brandnewdata.mop.poc.scene.bo.export.ConnectorExportBo;
import com.brandnewdata.mop.poc.scene.dto.external.ConnectorConfigDto;

public class ConnectorConfigDtoConverter {
    public static ConnectorConfigDto createFrom(ConnectorExportBo connectorExportBo, ConfigExportBo configExportBo) {
        ConnectorConfigDto ret = new ConnectorConfigDto();
        ret.setConnectorGroup(connectorExportBo.getConnectorGroup());
        ret.setConnectorId(connectorExportBo.getConnectorId());
        ret.setConnectorVersion(connectorExportBo.getConnectorVersion());
        ret.setConnectorName(connectorExportBo.getConnectorName());
        ret.setConfigId(configExportBo.getConfigId());
        ret.setConfigName(configExportBo.getConfigName());
        return ret;
    }
}
