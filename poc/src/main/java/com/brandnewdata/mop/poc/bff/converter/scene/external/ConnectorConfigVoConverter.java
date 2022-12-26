package com.brandnewdata.mop.poc.bff.converter.scene.external;

import com.brandnewdata.mop.poc.bff.vo.scene.external.ConnectorConfigVo;
import com.brandnewdata.mop.poc.scene.dto.external.ConnectorConfigDto;

public class ConnectorConfigVoConverter {

    public static ConnectorConfigVo createFrom(ConnectorConfigDto dto) {
        ConnectorConfigVo vo = new ConnectorConfigVo();
        vo.setConnectorGroup(dto.getConnectorGroup());
        vo.setConnectorId(dto.getConnectorId());
        vo.setConnectorName(dto.getConnectorName());
        vo.setConnectorVersion(dto.getConnectorVersion());
        vo.setConnectorIcon(dto.getConnectorIcon());
        vo.setConfigureName(dto.getConfigName());
        vo.setConfigureId(dto.getConfigId());
        return vo;
    }
}
