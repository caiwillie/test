package com.brandnewdata.mop.poc.scene.dto.external;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrepareLoadDto {

    private Long id;

    private List<ConnectorConfigDto> configs;
}
