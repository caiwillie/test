/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DtoCreator {
    public static <T extends CreatableFromEntity<T, E>, E> T create(E from, Class<T> clazz) {
        if (from == null) {
            return null;
        }
        try {
            CreatableFromEntity newDto = (CreatableFromEntity)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            newDto.fillFrom(from);
            return (T)newDto;
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new OperateRuntimeException("Not implemented");
        }
    }

    public static <T extends CreatableFromEntity<T, E>, E> List<T> create(List<E> entities, Class<T> clazz) {
        if (entities != null) return entities.stream().filter(item -> item != null).map(item -> DtoCreator.create(item, clazz)).collect(Collectors.toList());
        return new ArrayList();
    }
}
