package com.brandnewdata.mop.poc.operate.entity;

public abstract class OperateZeebeEntity<T extends OperateZeebeEntity<T>> extends OperateEntity<T> {
    private long key;
    private int partitionId;

    public long getKey() {
        return key;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public T setKey(long key) {
        this.key = key;
        return (T) this;
    }

    public T setPartitionId(int partitionId) {
        this.partitionId = partitionId;
        return (T) this;
    }
}
