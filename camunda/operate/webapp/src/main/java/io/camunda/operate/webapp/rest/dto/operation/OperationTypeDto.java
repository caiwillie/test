package io.camunda.operate.webapp.rest.dto.operation;

import io.camunda.operate.entities.OperationType;

public enum OperationTypeDto {
   RESOLVE_INCIDENT,
   CANCEL_PROCESS_INSTANCE,
   DELETE_PROCESS_INSTANCE,
   ADD_VARIABLE,
   UPDATE_VARIABLE,
   UNSPECIFIED,
   UNKNOWN;

   public static OperationTypeDto getType(OperationType type) {
      if (type == null) {
         return UNSPECIFIED;
      } else {
         OperationTypeDto typeDto = valueOf(type.name());
         return typeDto == null ? UNKNOWN : typeDto;
      }
   }
}
