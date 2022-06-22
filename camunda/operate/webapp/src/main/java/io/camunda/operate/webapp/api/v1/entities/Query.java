/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.entities.Query$Sort
 */
package io.camunda.operate.webapp.api.v1.entities;

import io.camunda.operate.webapp.api.v1.entities.Query;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Query<T> {
    private T filter;
    private int size = 10;
    private Object[] searchAfter = null;
    private List<Sort> sort = null;

    public int getSize() {
        return this.size;
    }

    public Query<T> setSize(int size) {
        this.size = size;
        return this;
    }

    public Object[] getSearchAfter() {
        return this.searchAfter;
    }

    public Query<T> setSearchAfter(Object[] searchAfter) {
        this.searchAfter = searchAfter;
        return this;
    }

    public List<Sort> getSort() {
        return this.sort;
    }

    public Query<T> setSort(List<Sort> sort) {
        this.sort = sort;
        return this;
    }

    public T getFilter() {
        return this.filter;
    }

    public Query<T> setFilter(T filter) {
        this.filter = filter;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Query query = (Query)o;
        return this.size == query.size && Objects.equals(this.filter, query.filter) && Arrays.equals(this.searchAfter, query.searchAfter) && Objects.equals(this.sort, query.sort);
    }

    public int hashCode() {
        int result = Objects.hash(this.filter, this.size, this.sort);
        result = 31 * result + Arrays.hashCode(this.searchAfter);
        return result;
    }

    public String toString() {
        return "Query{filter=" + this.filter + ", size=" + this.size + ", searchAfter=" + Arrays.toString(this.searchAfter) + ", sort=" + this.sort + "}";
    }


    public static class Sort {
        private String field;
        private Order order = Order.ASC;

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
            return new Sort().setField(field).setOrder(order);
        }

        public static Sort of(String field) {
            return Sort.of(field, Order.ASC);
        }

        public static List<Sort> listOf(String field, Order order) {
            return List.of(Sort.of(field, order));
        }

        public static List<Sort> listOf(String field) {
            return List.of(Sort.of(field));
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) return false;
            if (this.getClass() != o.getClass()) {
                return false;
            }
            Sort sort = (Sort)o;
            return Objects.equals(this.field, sort.field) && this.order == sort.order;
        }

        public int hashCode() {
            return Objects.hash(this.field, this.order);
        }


        public static enum Order {
            ASC,
            DESC;

        }
    }
}
