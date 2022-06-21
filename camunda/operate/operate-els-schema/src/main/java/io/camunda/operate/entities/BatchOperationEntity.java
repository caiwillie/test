package io.camunda.operate.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BatchOperationEntity extends OperateEntity {
   private String name;
   private OperationType type;
   private OffsetDateTime startDate;
   private OffsetDateTime endDate;
   private String username;
   private Integer instancesCount = 0;
   private Integer operationsTotalCount = 0;
   private Integer operationsFinishedCount = 0;
   @JsonIgnore
   private Object[] sortValues;

   public String getName() {
      return this.name;
   }

   public BatchOperationEntity setName(String name) {
      this.name = name;
      return this;
   }

   public OperationType getType() {
      return this.type;
   }

   public BatchOperationEntity setType(OperationType type) {
      this.type = type;
      return this;
   }

   public OffsetDateTime getStartDate() {
      return this.startDate;
   }

   public BatchOperationEntity setStartDate(OffsetDateTime startDate) {
      this.startDate = startDate;
      return this;
   }

   public OffsetDateTime getEndDate() {
      return this.endDate;
   }

   public BatchOperationEntity setEndDate(OffsetDateTime endDate) {
      this.endDate = endDate;
      return this;
   }

   public String getUsername() {
      return this.username;
   }

   public BatchOperationEntity setUsername(String username) {
      this.username = username;
      return this;
   }

   public Integer getInstancesCount() {
      return this.instancesCount;
   }

   public BatchOperationEntity setInstancesCount(Integer instancesCount) {
      this.instancesCount = instancesCount;
      return this;
   }

   public Integer getOperationsTotalCount() {
      return this.operationsTotalCount;
   }

   public BatchOperationEntity setOperationsTotalCount(Integer operationsTotalCount) {
      this.operationsTotalCount = operationsTotalCount;
      return this;
   }

   public Integer getOperationsFinishedCount() {
      return this.operationsFinishedCount;
   }

   public BatchOperationEntity setOperationsFinishedCount(Integer operationsFinishedCount) {
      this.operationsFinishedCount = operationsFinishedCount;
      return this;
   }

   public Object[] getSortValues() {
      return this.sortValues;
   }

   public BatchOperationEntity setSortValues(Object[] sortValues) {
      this.sortValues = sortValues;
      return this;
   }

   public void generateId() {
      this.setId(UUID.randomUUID().toString());
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            BatchOperationEntity that;
            label96: {
               that = (BatchOperationEntity)o;
               if (this.name != null) {
                  if (this.name.equals(that.name)) {
                     break label96;
                  }
               } else if (that.name == null) {
                  break label96;
               }

               return false;
            }

            if (this.type != that.type) {
               return false;
            } else {
               label88: {
                  if (this.startDate != null) {
                     if (this.startDate.equals(that.startDate)) {
                        break label88;
                     }
                  } else if (that.startDate == null) {
                     break label88;
                  }

                  return false;
               }

               if (this.endDate != null) {
                  if (!this.endDate.equals(that.endDate)) {
                     return false;
                  }
               } else if (that.endDate != null) {
                  return false;
               }

               label74: {
                  if (this.username != null) {
                     if (this.username.equals(that.username)) {
                        break label74;
                     }
                  } else if (that.username == null) {
                     break label74;
                  }

                  return false;
               }

               if (this.instancesCount != null) {
                  if (!this.instancesCount.equals(that.instancesCount)) {
                     return false;
                  }
               } else if (that.instancesCount != null) {
                  return false;
               }

               if (this.operationsTotalCount != null) {
                  if (this.operationsTotalCount.equals(that.operationsTotalCount)) {
                     return this.operationsFinishedCount != null ? this.operationsFinishedCount.equals(that.operationsFinishedCount) : that.operationsFinishedCount == null;
                  }
               } else if (that.operationsTotalCount == null) {
                  return this.operationsFinishedCount != null ? this.operationsFinishedCount.equals(that.operationsFinishedCount) : that.operationsFinishedCount == null;
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
      result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
      result = 31 * result + (this.startDate != null ? this.startDate.hashCode() : 0);
      result = 31 * result + (this.endDate != null ? this.endDate.hashCode() : 0);
      result = 31 * result + (this.username != null ? this.username.hashCode() : 0);
      result = 31 * result + (this.instancesCount != null ? this.instancesCount.hashCode() : 0);
      result = 31 * result + (this.operationsTotalCount != null ? this.operationsTotalCount.hashCode() : 0);
      result = 31 * result + (this.operationsFinishedCount != null ? this.operationsFinishedCount.hashCode() : 0);
      return result;
   }
}
