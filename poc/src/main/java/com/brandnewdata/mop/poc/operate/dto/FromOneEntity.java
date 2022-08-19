package com.brandnewdata.mop.poc.operate.dto;

public interface FromOneEntity<D extends FromOneEntity<D, E>, E> {
    D fromEntity(E entity);
}
