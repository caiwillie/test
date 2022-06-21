package io.camunda.operate.webapp.rest.dto.incidents;

import java.util.ArrayList;
import java.util.List;

public class IncidentResponseDto {
   private long count;
   private List incidents = new ArrayList();
   private List errorTypes = new ArrayList();
   private List flowNodes = new ArrayList();

   public long getCount() {
      return this.count;
   }

   public void setCount(long count) {
      this.count = count;
   }

   public List getIncidents() {
      return this.incidents;
   }

   public void setIncidents(List incidents) {
      this.incidents = incidents;
   }

   public List getErrorTypes() {
      return this.errorTypes;
   }

   public void setErrorTypes(List errorTypes) {
      this.errorTypes = errorTypes;
   }

   public List getFlowNodes() {
      return this.flowNodes;
   }

   public void setFlowNodes(List flowNodes) {
      this.flowNodes = flowNodes;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         IncidentResponseDto that = (IncidentResponseDto)o;
         if (this.count != that.count) {
            return false;
         } else {
            label44: {
               if (this.incidents != null) {
                  if (this.incidents.equals(that.incidents)) {
                     break label44;
                  }
               } else if (that.incidents == null) {
                  break label44;
               }

               return false;
            }

            if (this.errorTypes != null) {
               if (!this.errorTypes.equals(that.errorTypes)) {
                  return false;
               }
            } else if (that.errorTypes != null) {
               return false;
            }

            return this.flowNodes != null ? this.flowNodes.equals(that.flowNodes) : that.flowNodes == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = (int)(this.count ^ this.count >>> 32);
      result = 31 * result + (this.incidents != null ? this.incidents.hashCode() : 0);
      result = 31 * result + (this.errorTypes != null ? this.errorTypes.hashCode() : 0);
      result = 31 * result + (this.flowNodes != null ? this.flowNodes.hashCode() : 0);
      return result;
   }
}
