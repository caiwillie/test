package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.exceptions.OperateRuntimeException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DtoCreator {
   public static CreatableFromEntity create(Object from, Class clazz) {
      if (from == null) {
         return null;
      } else {
         try {
            CreatableFromEntity newDto = (CreatableFromEntity)clazz.getDeclaredConstructor().newInstance();
            newDto.fillFrom(from);
            return newDto;
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException var3) {
            throw new OperateRuntimeException("Not implemented");
         }
      }
   }

   public static List create(List entities, Class clazz) {
      return (List)(entities == null ? new ArrayList() : (List)entities.stream().filter((item) -> {
         return item != null;
      }).map((item) -> {
         return create(item, clazz);
      }).collect(Collectors.toList()));
   }
}
