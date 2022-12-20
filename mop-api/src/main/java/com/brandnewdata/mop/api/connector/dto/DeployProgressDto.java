package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployProgressDto {

    private boolean success;

    private String message;
}
