package io.camunda.operate.webapp.rest.dto.listview;

import java.util.ArrayList;
import java.util.List;

public class ListViewResponseDto {
   private List processInstances = new ArrayList();
   private long totalCount;

   public List getProcessInstances() {
      return this.processInstances;
   }

   public void setProcessInstances(List processInstances) {
      this.processInstances = processInstances;
   }

   public long getTotalCount() {
      return this.totalCount;
   }

   public void setTotalCount(long totalCount) {
      this.totalCount = totalCount;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ListViewResponseDto that = (ListViewResponseDto)o;
         if (this.totalCount != that.totalCount) {
            return false;
         } else {
            return this.processInstances != null ? this.processInstances.equals(that.processInstances) : that.processInstances == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.processInstances != null ? this.processInstances.hashCode() : 0;
      result = 31 * result + (int)(this.totalCount ^ this.totalCount >>> 32);
      return result;
   }
}
