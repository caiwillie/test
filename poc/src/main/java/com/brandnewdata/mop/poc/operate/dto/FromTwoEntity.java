package com.brandnewdata.mop.poc.operate.dto;

public interface FromTwoEntity<D extends FromTwoEntity<D, E1, E2>, E1, E2> {
    D fromEntity(E1 entity1, E2 entity2);
}
