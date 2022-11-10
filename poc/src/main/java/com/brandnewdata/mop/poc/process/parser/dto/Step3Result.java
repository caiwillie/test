package com.brandnewdata.mop.poc.process.parser.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Step3Result extends Step2Result {

    private Action trigger;

    private String protocol;

    private ObjectNode requestParams;

    private ObjectNode responseParams;
}
