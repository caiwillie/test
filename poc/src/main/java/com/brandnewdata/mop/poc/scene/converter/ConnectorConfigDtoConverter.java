package com.brandnewdata.mop.poc.scene.converter;

import com.brandnewdata.mop.poc.process.manager.dto.ConfigInfo;
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
        ret.setConnectorIcon(connectorExportBo.getConnectorSmallIcon());
        ret.setConfigId(configExportBo.getConfigId());
        ret.setConfigName(configExportBo.getConfigName());
        return ret;
    }

    public static ConnectorConfigDto createFrom(ConfigInfo configInfo) {
        ConnectorConfigDto ret = new ConnectorConfigDto();
        ret.setConnectorGroup(configInfo.getConnectorGroup());
        ret.setConnectorId(configInfo.getConnectorId());
        ret.setConnectorVersion(configInfo.getConnectorVersion());
        ret.setConfigId(String.valueOf(configInfo.getId()));
        ret.setConfigName(configInfo.getConfigName());
        return ret;
    }
}
