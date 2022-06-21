package io.camunda.operate.webapp.rest.dto.activity;

import java.util.List;
import java.util.Objects;

public class FlowNodeInstanceResponseDto {
   private Boolean isRunning;
   private List children;

   public FlowNodeInstanceResponseDto() {
   }

   public FlowNodeInstanceResponseDto(Boolean running, List children) {
      this.isRunning = running;
      this.children = children;
   }

   public List getChildren() {
      return this.children;
   }

   public FlowNodeInstanceResponseDto setChildren(List children) {
      this.children = children;
      return this;
   }

   public Boolean getRunning() {
      return this.isRunning;
   }

   public FlowNodeInstanceResponseDto setRunning(Boolean running) {
      this.isRunning = running;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FlowNodeInstanceResponseDto that = (FlowNodeInstanceResponseDto)o;
         return Objects.equals(this.isRunning, that.isRunning) && Objects.equals(this.children, that.children);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.isRunning, this.children});
   }
}
