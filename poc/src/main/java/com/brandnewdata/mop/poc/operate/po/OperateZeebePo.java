package com.brandnewdata.mop.poc.operate.po;

public abstract class OperateZeebePo<T extends OperateZeebePo<T>> extends OperatePo<T> {
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
