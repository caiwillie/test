package io.camunda.operate.webapp.api.v1.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Results {
   private List items = new ArrayList();
   private Object[] sortValues = new Object[0];
   private long total;

   public long getTotal() {
      return this.total;
   }

   public Results setTotal(long total) {
      this.total = total;
      return this;
   }

   public List getItems() {
      return this.items;
   }

   public Results setItems(List items) {
      this.items = items;
      return this;
   }

   public Object[] getSortValues() {
      return this.sortValues;
   }

   public Results setSortValues(Object[] sortValues) {
      this.sortValues = sortValues;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Results results = (Results)o;
         return this.total == results.total && Objects.equals(this.items, results.items) && Arrays.equals(this.sortValues, results.sortValues);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.items, Arrays.hashCode(this.sortValues), this.total});
   }

   public String toString() {
      List var10000 = this.items;
      return "Results{items=" + var10000 + ", sortValues=" + Arrays.toString(this.sortValues) + ", total=" + this.total + "}";
   }
}
