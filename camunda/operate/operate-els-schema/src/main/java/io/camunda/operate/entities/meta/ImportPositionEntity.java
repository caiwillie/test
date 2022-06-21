package io.camunda.operate.entities.meta;

import io.camunda.operate.entities.OperateEntity;

public class ImportPositionEntity extends OperateEntity {
   private String aliasName;
   private int partitionId;
   private long position;
   private String indexName;

   public ImportPositionEntity() {
   }

   public ImportPositionEntity(String aliasName, int partitionId, long position) {
      this.aliasName = aliasName;
      this.partitionId = partitionId;
      this.position = position;
   }

   public String getAliasName() {
      return this.aliasName;
   }

   public void setAliasName(String aliasName) {
      this.aliasName = aliasName;
   }

   public int getPartitionId() {
      return this.partitionId;
   }

   public void setPartitionId(int partitionId) {
      this.partitionId = partitionId;
   }

   public long getPosition() {
      return this.position;
   }

   public void setPosition(long position) {
      this.position = position;
   }

   public String getIndexName() {
      return this.indexName;
   }

   public void setIndexName(String indexName) {
      this.indexName = indexName;
   }

   public String getId() {
      return String.format("%s-%s", this.partitionId, this.aliasName);
   }

   public static ImportPositionEntity createFrom(ImportPositionEntity importPositionEntity, long newPosition, String indexName) {
      ImportPositionEntity newImportPositionEntity = new ImportPositionEntity();
      newImportPositionEntity.setAliasName(importPositionEntity.getAliasName());
      newImportPositionEntity.setPartitionId(importPositionEntity.getPartitionId());
      newImportPositionEntity.setIndexName(indexName);
      newImportPositionEntity.setPosition(newPosition);
      return newImportPositionEntity;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            ImportPositionEntity that = (ImportPositionEntity)o;
            if (this.partitionId != that.partitionId) {
               return false;
            } else if (this.position != that.position) {
               return false;
            } else {
               if (this.aliasName != null) {
                  if (!this.aliasName.equals(that.aliasName)) {
                     return false;
                  }
               } else if (that.aliasName != null) {
                  return false;
               }

               return this.indexName != null ? this.indexName.equals(that.indexName) : that.indexName == null;
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.aliasName != null ? this.aliasName.hashCode() : 0);
      result = 31 * result + this.partitionId;
      result = 31 * result + (int)(this.position ^ this.position >>> 32);
      result = 31 * result + (this.indexName != null ? this.indexName.hashCode() : 0);
      return result;
   }

   public String toString() {
      return "ImportPositionEntity{aliasName='" + this.aliasName + "', partitionId=" + this.partitionId + ", position=" + this.position + ", indexName='" + this.indexName + "'}";
   }
}
