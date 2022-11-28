package com.brandnewdata.mop.poc.operate.schema.template;

import org.springframework.stereotype.Component;

public abstract class AbstractTemplateDescriptor implements TemplateDescriptor {

    private static final String INDEX_PREFIX = "operate";

    public AbstractTemplateDescriptor() {
    }

    public String getFullQualifiedName() {
        return String.format("%s-%s-%s_", INDEX_PREFIX, this.getIndexName(), this.getVersion());
    }

}
