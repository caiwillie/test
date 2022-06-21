package io.camunda.operate.schema.migration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.OffsetDateTime;

@JsonTypeName("processorStep")
public class ProcessorStep implements Step {
   private String content;
   private String description;
   private OffsetDateTime createdDate;
   private OffsetDateTime appliedDate;
   private String indexName;
   private boolean isApplied = false;
   private String version;
   private Integer order = 0;

   public boolean isApplied() {
      return this.isApplied;
   }

   public Step setApplied(boolean isApplied) {
      this.isApplied = isApplied;
      return this;
   }

   public String getIndexName() {
      return this.indexName;
   }

   public String getContent() {
      return this.content;
   }

   public String getDescription() {
      return this.description;
   }

   public String getVersion() {
      return this.version;
   }

   public Integer getOrder() {
      return this.order;
   }

   public OffsetDateTime getCreatedDate() {
      if (this.createdDate == null) {
         this.createdDate = OffsetDateTime.now();
      }

      return this.createdDate;
   }

   public Step setCreatedDate(OffsetDateTime createDate) {
      this.createdDate = createDate;
      return this;
   }

   public OffsetDateTime getAppliedDate() {
      return this.appliedDate;
   }

   public Step setAppliedDate(OffsetDateTime appliedDate) {
      this.appliedDate = appliedDate;
      return this;
   }

   public int hashCode() {
      boolean prime = true;
      int result = 1;
      result = 31 * result + (this.content == null ? 0 : this.content.hashCode());
      result = 31 * result + (this.indexName == null ? 0 : this.indexName.hashCode());
      result = 31 * result + (this.order == null ? 0 : this.order.hashCode());
      result = 31 * result + (this.version == null ? 0 : this.version.hashCode());
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         ProcessorStep other = (ProcessorStep)obj;
         if (this.content == null) {
            if (other.content != null) {
               return false;
            }
         } else if (!this.content.equals(other.content)) {
            return false;
         }

         if (this.indexName == null) {
            if (other.indexName != null) {
               return false;
            }
         } else if (!this.indexName.equals(other.indexName)) {
            return false;
         }

         if (this.order == null) {
            if (other.order != null) {
               return false;
            }
         } else if (!this.order.equals(other.order)) {
            return false;
         }

         if (this.version == null) {
            if (other.version != null) {
               return false;
            }
         } else if (!this.version.equals(other.version)) {
            return false;
         }

         return true;
      }
   }

   public String toString() {
      String var10000 = this.content;
      return "ProcessorStep [content=" + var10000 + ", appliedDate=" + this.appliedDate + ", indexName=" + this.indexName + ", isApplied=" + this.isApplied + ", version=" + this.version + ", order=" + this.order + ", createdDate=" + this.getCreatedDate() + "]";
   }
}
