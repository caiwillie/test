/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.rest.dto.listview;

public class VariablesQueryDto {
    private String name;
    private String value;

    public VariablesQueryDto() {
    }

    public VariablesQueryDto(String variableName, String variableValue) {
        this.name = variableName;
        this.value = variableValue;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        VariablesQueryDto that = (VariablesQueryDto)o;
        if (!(this.name != null ? !this.name.equals(that.name) : that.name != null)) return this.value != null ? this.value.equals(that.value) : that.value == null;
        return false;
    }

    public int hashCode() {
        int result = this.name != null ? this.name.hashCode() : 0;
        result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }
}
