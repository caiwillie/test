package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.DecisionInstanceState;

public enum DecisionInstanceStateDto {
   FAILED,
   EVALUATED,
   UNKNOWN,
   UNSPECIFIED;

   public static DecisionInstanceStateDto getState(DecisionInstanceState state) {
      if (state == null) {
         return UNSPECIFIED;
      } else {
         DecisionInstanceStateDto stateDto = valueOf(state.name());
         return stateDto == null ? UNKNOWN : stateDto;
      }
   }
}
