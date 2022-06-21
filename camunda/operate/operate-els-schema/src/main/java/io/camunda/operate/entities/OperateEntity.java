package io.camunda.operate.entities;

public abstract class OperateEntity {
   private String id;

   public String getId() {
      return this.id;
   }

   public OperateEntity setId(String id) {
      this.id = id;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         OperateEntity that = (OperateEntity)o;
         return this.id != null ? this.id.equals(that.id) : that.id == null;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.id != null ? this.id.hashCode() : 0;
   }

   public String toString() {
      return "OperateEntity{id='" + this.id + "'}";
   }
}
