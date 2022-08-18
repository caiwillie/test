package com.brandnewdata.mop.poc.operate.dto;

public interface ToEntity<D extends ToEntity<D, E>, E> {

    E toEntity(D dto);
}
