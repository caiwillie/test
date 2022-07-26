package com.brandnewdata.mop.poc.process.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.dom4j.Element;

@Data
public class BndStartEvent {
    private Element callActivity;
    private ObjectNode inputs;
}
