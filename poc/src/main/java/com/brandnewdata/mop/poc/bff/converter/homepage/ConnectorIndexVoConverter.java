package com.brandnewdata.mop.poc.bff.converter.homepage;

import com.brandnewdata.connector.dto.ConnectorBasicListInfoDTO;
import com.brandnewdata.mop.poc.bff.vo.homepage.ConnectorIndexVo;

/**
 * @author jekyll 2022-12-16 18:00
 */
public class ConnectorIndexVoConverter {

    public static ConnectorIndexVo createForm(ConnectorBasicListInfoDTO dto){
        ConnectorIndexVo connectorIndexVo = new ConnectorIndexVo();

        connectorIndexVo.setFirm(dto.getConnectorCompany());
        connectorIndexVo.setIcon(dto.getConnectorBigIcon());
        connectorIndexVo.setId(dto.getConnectorId());
        connectorIndexVo.setName(dto.getConnectorName());
        connectorIndexVo.setVersion(dto.getConnectorVersion());
        connectorIndexVo.setConnectorType(dto.getConnectorType());
        return connectorIndexVo;
    }
}
