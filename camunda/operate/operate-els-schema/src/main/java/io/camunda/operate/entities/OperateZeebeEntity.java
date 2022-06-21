package io.camunda.operate.entities;

public abstract class OperateZeebeEntity extends OperateEntity {
   private long key;
   private int partitionId;

   public OperateZeebeEntity setKey(long key) {
      this.key = key;
      return this;
   }

   public OperateZeebeEntity setPartitionId(int partitionId) {
      this.partitionId = partitionId;
      return this;
   }

   public long getKey() {
      return this.key;
   }

   public int getPartitionId() {
      return this.partitionId;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            OperateZeebeEntity that = (OperateZeebeEntity)o;
            if (this.key != that.key) {
               return false;
            } else {
               return this.partitionId == that.partitionId;
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (int)(this.key ^ this.key >>> 32);
      result = 31 * result + this.partitionId;
      return result;
   }
}
