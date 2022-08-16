package com.brandnewdata.mop.poc.operate.schema.index;

public interface IndexDescriptor {
    String getIndexName();

    String getFullQualifiedName();

    String getVersion();

    default String getDerivedIndexNamePattern() {
        return this.getFullQualifiedName() + "*";
    }

    default String getAlias() {
        return this.getFullQualifiedName() + "alias";
    }

}
