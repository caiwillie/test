package com.brandnewdata.mop.poc.scene.dto.external;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AllExternal {
    Map<Long, SceneProcessExternal> processMap;
    Map<String, ProcessDefinitionExternal> definitionMap;
    Map<String, ConfigExternal> configMap;
}
