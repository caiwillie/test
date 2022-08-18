package com.brandnewdata.mop.poc.operate.dto;

public interface FromEntity<D extends FromEntity<D, E>, E> {
    D fromEntity(E entity);
}
