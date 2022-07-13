package com.brandnewdata.mop.poc.parser;

import lombok.Data;

@Data
public class IOMap {

    /**
     * zeebe source
     */
    private String source;

    /**
     * zeebe target
     */
    private String target;

    public IOMap(String source, String target) {
        this.source = source;
        this.target = target;
    }
}
