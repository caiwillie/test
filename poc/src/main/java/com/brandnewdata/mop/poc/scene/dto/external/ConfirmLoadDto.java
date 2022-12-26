package com.brandnewdata.mop.poc.scene.dto.external;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ConfirmLoadDto {
    private Long id;

    private String newSceneName;

    private Map<String, String> configMap;
}
