package com.brandnewdata.mop.poc.operate.entity;

import lombok.Data;

public abstract class OperateEntity<T extends OperateEntity<T>> {

    private String id;

    public String getId() {
        return id;
    }

    public T setId(String id) {
        this.id = id;
        return (T) this;
    }

}
