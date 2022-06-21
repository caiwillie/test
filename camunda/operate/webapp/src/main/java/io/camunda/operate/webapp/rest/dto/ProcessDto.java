package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.ProcessEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Process object")
public class ProcessDto implements CreatableFromEntity<ProcessEntity> {
   @ApiModelProperty("Unique id of the process, must be used when filtering instances by process ids.")
   private String id;
   private String name;
   private int version;
   private String bpmnProcessId;

   public String getId() {
      return this.id;
   }

   public ProcessDto setId(String id) {
      this.id = id;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public ProcessDto setName(String name) {
      this.name = name;
      return this;
   }

   public int getVersion() {
      return this.version;
   }

   public ProcessDto setVersion(int version) {
      this.version = version;
      return this;
   }

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public ProcessDto setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
      return this;
   }

   @Override
   public ProcessDto fillFrom(ProcessEntity processEntity) {
      this.setId(processEntity.getId()).setBpmnProcessId(processEntity.getBpmnProcessId()).setName(processEntity.getName()).setVersion(processEntity.getVersion());
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ProcessDto that = (ProcessDto)o;
         if (this.version != that.version) {
            return false;
         } else {
            label44: {
               if (this.id != null) {
                  if (this.id.equals(that.id)) {
                     break label44;
                  }
               } else if (that.id == null) {
                  break label44;
               }

               return false;
            }

            if (this.name != null) {
               if (!this.name.equals(that.name)) {
                  return false;
               }
            } else if (that.name != null) {
               return false;
            }

            return this.bpmnProcessId != null ? this.bpmnProcessId.equals(that.bpmnProcessId) : that.bpmnProcessId == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.id != null ? this.id.hashCode() : 0;
      result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
      result = 31 * result + this.version;
      result = 31 * result + (this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0);
      return result;
   }

}
