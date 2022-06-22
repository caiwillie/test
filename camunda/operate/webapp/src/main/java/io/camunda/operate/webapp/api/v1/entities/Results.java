/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.api.v1.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Results<T> {
    private List<T> items = new ArrayList<T>();
    private Object[] sortValues = new Object[0];
    private long total;

    public long getTotal() {
        return this.total;
    }

    public Results<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    public List<T> getItems() {
        return this.items;
    }

    public Results<T> setItems(List<T> items) {
        this.items = items;
        return this;
    }

    public Object[] getSortValues() {
        return this.sortValues;
    }

    public Results<T> setSortValues(Object[] sortValues) {
        this.sortValues = sortValues;
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
        Results results = (Results)o;
        return this.total == results.total && Objects.equals(this.items, results.items) && Arrays.equals(this.sortValues, results.sortValues);
    }

    public int hashCode() {
        return Objects.hash(this.items, Arrays.hashCode(this.sortValues), this.total);
    }

    public String toString() {
        return "Results{items=" + this.items + ", sortValues=" + Arrays.toString(this.sortValues) + ", total=" + this.total + "}";
    }
}
