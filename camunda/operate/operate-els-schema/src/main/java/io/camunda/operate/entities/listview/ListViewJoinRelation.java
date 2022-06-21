package io.camunda.operate.entities.listview;

public class ListViewJoinRelation {
   private String name;
   private Long parent;

   public ListViewJoinRelation() {
   }

   public ListViewJoinRelation(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Long getParent() {
      return this.parent;
   }

   public void setParent(Long parent) {
      this.parent = parent;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ListViewJoinRelation that = (ListViewJoinRelation)o;
         if (this.name != null) {
            if (this.name.equals(that.name)) {
               return this.parent != null ? this.parent.equals(that.parent) : that.parent == null;
            }
         } else if (that.name == null) {
            return this.parent != null ? this.parent.equals(that.parent) : that.parent == null;
         }

         return false;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.name != null ? this.name.hashCode() : 0;
      result = 31 * result + (this.parent != null ? this.parent.hashCode() : 0);
      return result;
   }
}
