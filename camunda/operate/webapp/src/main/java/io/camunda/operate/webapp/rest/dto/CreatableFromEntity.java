/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.rest.dto;

public interface CreatableFromEntity<T extends CreatableFromEntity<T, E>, E> {
    public T fillFrom(E var1);
}
