package com.brandnewdata.mop.poc.operate.po;

public abstract class OperatePo<T extends OperatePo<T>> {

    private String id;

    public String getId() {
        return id;
    }

    public T setId(String id) {
        this.id = id;
        return (T) this;
    }

}
