package com.brandnewdata.mop.poc.operate.schema.index;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractIndexDescriptor implements IndexDescriptor {
    public static final String PARTITION_ID = "partitionId";

    public AbstractIndexDescriptor() {
    }

    public String getFullQualifiedName() {
        return String.format("%s-%s-%s_", "operate", this.getIndexName(), this.getVersion());
    }
}
