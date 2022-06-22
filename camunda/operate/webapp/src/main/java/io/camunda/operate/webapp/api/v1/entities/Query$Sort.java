/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.entities.Query$Sort$Order
 */
package io.camunda.operate.webapp.api.v1.entities;

import io.camunda.operate.webapp.api.v1.entities.Query;
import java.util.List;
import java.util.Objects;
/*

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
}
*/
