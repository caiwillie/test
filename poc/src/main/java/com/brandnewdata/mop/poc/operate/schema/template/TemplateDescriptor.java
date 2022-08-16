package com.brandnewdata.mop.poc.operate.schema.template;

import com.brandnewdata.mop.poc.operate.schema.index.IndexDescriptor;

public interface TemplateDescriptor extends IndexDescriptor {

    String PARTITION_ID = "partitionId";

    default String getTemplateName() {
        return this.getFullQualifiedName() + "template";
    }

    default String getIndexPattern() {
        return this.getFullQualifiedName() + "*";
    }
}
