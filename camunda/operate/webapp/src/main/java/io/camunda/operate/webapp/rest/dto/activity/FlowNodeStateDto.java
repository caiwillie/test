package io.camunda.operate.webapp.rest.dto.activity;

import io.camunda.operate.entities.FlowNodeState;

public enum FlowNodeStateDto {
   ACTIVE,
   INCIDENT,
   COMPLETED,
   TERMINATED,
   UNSPECIFIED,
   UNKNOWN;

   public static FlowNodeStateDto getState(FlowNodeState state) {
      if (state == null) {
         return UNSPECIFIED;
      } else {
         FlowNodeStateDto stateDto = valueOf(state.name());
         return stateDto == null ? UNKNOWN : stateDto;
      }
   }
}
