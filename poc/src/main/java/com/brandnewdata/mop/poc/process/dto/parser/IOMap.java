package com.brandnewdata.mop.poc.process.dto.parser;

import lombok.Data;

@Data
public class IOMap {

    private String source;

    private String target;

    public IOMap(String source, String target) {
        this.source = source;
        this.target = target;
    }
}
