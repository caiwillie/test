package io.camunda.operate.webapp.rest.dto.listview;

import io.camunda.operate.entities.listview.ProcessInstanceState;

public enum ProcessInstanceStateDto {
   ACTIVE,
   INCIDENT,
   COMPLETED,
   CANCELED,
   UNKNOWN,
   UNSPECIFIED;

   public static ProcessInstanceStateDto getState(ProcessInstanceState state) {
      if (state == null) {
         return UNSPECIFIED;
      } else {
         ProcessInstanceStateDto stateDto = valueOf(state.name());
         return stateDto == null ? UNKNOWN : stateDto;
      }
   }
}
