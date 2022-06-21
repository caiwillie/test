package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.ProcessEntity;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@ApiModel(
   value = "Process group object",
   description = "Group of processes with the same bpmnProcessId with all versions included"
)
public class ProcessGroupDto {
   private String bpmnProcessId;
   private String name;
   private List processes;

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public void setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List getProcesses() {
      return this.processes;
   }

   public void setProcesses(List processes) {
      this.processes = processes;
   }

   public static List createFrom(Map processesGrouped) {
      List groups = new ArrayList();
      processesGrouped.entrySet().stream().forEach((groupEntry) -> {
         ProcessGroupDto groupDto = new ProcessGroupDto();
         groupDto.setBpmnProcessId((String)groupEntry.getKey());
         groupDto.setName(((ProcessEntity)((List)groupEntry.getValue()).get(0)).getName());
         groupDto.setProcesses(DtoCreator.create((List)groupEntry.getValue(), ProcessDto.class));
         groups.add(groupDto);
      });
      groups.sort(new ProcessGroupComparator());
      return groups;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ProcessGroupDto that = (ProcessGroupDto)o;
         return this.bpmnProcessId != null ? this.bpmnProcessId.equals(that.bpmnProcessId) : that.bpmnProcessId == null;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0;
   }

   public static class ProcessGroupComparator implements Comparator {
      public int compare(ProcessGroupDto o1, ProcessGroupDto o2) {
         if (o1.getName() == null && o2.getName() == null) {
            return o1.getBpmnProcessId().compareTo(o2.getBpmnProcessId());
         } else if (o1.getName() == null) {
            return 1;
         } else if (o2.getName() == null) {
            return -1;
         } else {
            return !o1.getName().equals(o2.getName()) ? o1.getName().compareTo(o2.getName()) : o1.getBpmnProcessId().compareTo(o2.getBpmnProcessId());
         }
      }
   }
}
