package com.brandnewdata.mop.poc.operate.dto;

public interface FromOneEntity<D extends FromOneEntity<D, E>, E> {
    D from(E entity);
}
