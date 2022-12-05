package com.brandnewdata.mop.api.process.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SendMessageDto {

    private Long envId;

    private String messageName;

    private String correlationKey;

    private Map<String, Object> variables;

}
