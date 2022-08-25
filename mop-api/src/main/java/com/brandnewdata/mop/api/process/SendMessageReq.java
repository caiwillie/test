package com.brandnewdata.mop.api.process;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SendMessageReq {

    private String messageName;

    private String correlationKey;

    private Map<String, Object> variables;

}
