package io.camunda.operate.webapp.rest.dto.incidents;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class IncidentsByErrorMsgStatisticsDto {
   public static final Comparator COMPARATOR = new IncidentsByErrorMsgStatisticsDtoComparator();
   private String errorMessage;
   private long instancesWithErrorCount;
   @JsonDeserialize(
      as = TreeSet.class
   )
   private Set processes = new TreeSet();

   public IncidentsByErrorMsgStatisticsDto() {
   }

   public IncidentsByErrorMsgStatisticsDto(String errorMessage) {
      this.errorMessage = errorMessage;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
   }

   public long getInstancesWithErrorCount() {
      return this.instancesWithErrorCount;
   }

   public void setInstancesWithErrorCount(long instancesWithErrorCount) {
      this.instancesWithErrorCount = instancesWithErrorCount;
   }

   public Set getProcesses() {
      return this.processes;
   }

   public void setProcesses(Set processes) {
      this.processes = processes;
   }

   public void recordInstancesCount(long count) {
      this.instancesWithErrorCount += count;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         IncidentsByErrorMsgStatisticsDto that = (IncidentsByErrorMsgStatisticsDto)o;
         if (this.instancesWithErrorCount != that.instancesWithErrorCount) {
            return false;
         } else {
            if (this.errorMessage != null) {
               if (this.errorMessage.equals(that.errorMessage)) {
                  return this.processes != null ? this.processes.equals(that.processes) : that.processes == null;
               }
            } else if (that.errorMessage == null) {
               return this.processes != null ? this.processes.equals(that.processes) : that.processes == null;
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.errorMessage != null ? this.errorMessage.hashCode() : 0;
      result = 31 * result + (int)(this.instancesWithErrorCount ^ this.instancesWithErrorCount >>> 32);
      result = 31 * result + (this.processes != null ? this.processes.hashCode() : 0);
      return result;
   }

   public static class IncidentsByErrorMsgStatisticsDtoComparator implements Comparator {
      public int compare(IncidentsByErrorMsgStatisticsDto o1, IncidentsByErrorMsgStatisticsDto o2) {
         if (o1 == null) {
            return o2 == null ? 0 : 1;
         } else if (o2 == null) {
            return -1;
         } else if (o1.equals(o2)) {
            return 0;
         } else {
            int result = Long.compare(o2.getInstancesWithErrorCount(), o1.getInstancesWithErrorCount());
            if (result == 0) {
               result = o1.getErrorMessage().compareTo(o2.getErrorMessage());
            }

            return result;
         }
      }
   }
}
