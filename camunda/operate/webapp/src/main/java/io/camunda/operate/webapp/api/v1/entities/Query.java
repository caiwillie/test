package io.camunda.operate.webapp.api.v1.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Query {
   private Object filter;
   private int size = 10;
   private Object[] searchAfter = null;
   private List<Sort> sort = null;

   public int getSize() {
      return this.size;
   }

   public Query setSize(int size) {
      this.size = size;
      return this;
   }

   public Object[] getSearchAfter() {
      return this.searchAfter;
   }

   public Query setSearchAfter(Object[] searchAfter) {
      this.searchAfter = searchAfter;
      return this;
   }

   public List<Sort> getSort() {
      return this.sort;
   }

   public Query setSort(List<Sort> sort) {
      this.sort = sort;
      return this;
   }

   public Object getFilter() {
      return this.filter;
   }

   public Query setFilter(Object filter) {
      this.filter = filter;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Query query = (Query)o;
         return this.size == query.size && Objects.equals(this.filter, query.filter) && Arrays.equals(this.searchAfter, query.searchAfter) && Objects.equals(this.sort, query.sort);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{this.filter, this.size, this.sort});
      result = 31 * result + Arrays.hashCode(this.searchAfter);
      return result;
   }

   public String toString() {
      Object var10000 = this.filter;
      return "Query{filter=" + var10000 + ", size=" + this.size + ", searchAfter=" + Arrays.toString(this.searchAfter) + ", sort=" + this.sort + "}";
   }

   public static class Sort {
      private String field;
      private Order order;

      public Sort() {
         this.order = Query.Sort.Order.ASC;
      }

      public String getField() {
         return this.field;
      }

      public Sort setField(String field) {
         this.field = field;
         return this;
      }

      public Order getOrder() {
         return this.order;
      }

      public Sort setOrder(Order order) {
         this.order = order;
         return this;
      }

      public static Sort of(String field, Order order) {
         return (new Sort()).setField(field).setOrder(order);
      }

      public static Sort of(String field) {
         return of(field, Query.Sort.Order.ASC);
      }

      public static List listOf(String field, Order order) {
         return List.of(of(field, order));
      }

      public static List listOf(String field) {
         return List.of(of(field));
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Sort sort = (Sort)o;
            return Objects.equals(this.field, sort.field) && this.order == sort.order;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.field, this.order});
      }

      public static enum Order {
         ASC,
         DESC;
      }
   }
}
