package io.camunda.operate.webapp.rest.dto.incidents;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class IncidentsByProcessGroupStatisticsDto {
   public static final Comparator COMPARATOR = new IncidentsByProcessGroupStatisticsDtoComparator();
   private String bpmnProcessId;
   private String processName;
   private long instancesWithActiveIncidentsCount;
   private long activeInstancesCount;
   @JsonDeserialize(
      as = TreeSet.class
   )
   private Set processes;

   public IncidentsByProcessGroupStatisticsDto() {
      this.processes = new TreeSet(IncidentByProcessStatisticsDto.COMPARATOR);
   }

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public void setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
   }

   public String getProcessName() {
      return this.processName;
   }

   public void setProcessName(String processName) {
      this.processName = processName;
   }

   public long getInstancesWithActiveIncidentsCount() {
      return this.instancesWithActiveIncidentsCount;
   }

   public void setInstancesWithActiveIncidentsCount(long instancesWithActiveIncidentsCount) {
      this.instancesWithActiveIncidentsCount = instancesWithActiveIncidentsCount;
   }

   public long getActiveInstancesCount() {
      return this.activeInstancesCount;
   }

   public void setActiveInstancesCount(long activeInstancesCount) {
      this.activeInstancesCount = activeInstancesCount;
   }

   public Set getProcesses() {
      return this.processes;
   }

   public void setProcesses(Set processes) {
      this.processes = processes;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         IncidentsByProcessGroupStatisticsDto that = (IncidentsByProcessGroupStatisticsDto)o;
         if (this.instancesWithActiveIncidentsCount != that.instancesWithActiveIncidentsCount) {
            return false;
         } else if (this.activeInstancesCount != that.activeInstancesCount) {
            return false;
         } else {
            label46: {
               if (this.bpmnProcessId != null) {
                  if (this.bpmnProcessId.equals(that.bpmnProcessId)) {
                     break label46;
                  }
               } else if (that.bpmnProcessId == null) {
                  break label46;
               }

               return false;
            }

            if (this.processName != null) {
               if (!this.processName.equals(that.processName)) {
                  return false;
               }
            } else if (that.processName != null) {
               return false;
            }

            return this.processes != null ? this.processes.equals(that.processes) : that.processes == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0;
      result = 31 * result + (this.processName != null ? this.processName.hashCode() : 0);
      result = 31 * result + (int)(this.instancesWithActiveIncidentsCount ^ this.instancesWithActiveIncidentsCount >>> 32);
      result = 31 * result + (int)(this.activeInstancesCount ^ this.activeInstancesCount >>> 32);
      result = 31 * result + (this.processes != null ? this.processes.hashCode() : 0);
      return result;
   }

   public static class IncidentsByProcessGroupStatisticsDtoComparator implements Comparator {
      public int compare(IncidentsByProcessGroupStatisticsDto o1, IncidentsByProcessGroupStatisticsDto o2) {
         if (o1 == null) {
            return o2 == null ? 0 : 1;
         } else if (o2 == null) {
            return -1;
         } else if (o1.equals(o2)) {
            return 0;
         } else {
            int result = Long.compare(o2.getInstancesWithActiveIncidentsCount(), o1.getInstancesWithActiveIncidentsCount());
            if (result == 0) {
               result = Long.compare(o2.getActiveInstancesCount(), o1.getActiveInstancesCount());
               if (result == 0) {
                  result = o1.getBpmnProcessId().compareTo(o2.getBpmnProcessId());
               }
            }

            return result;
         }
      }
   }
}
