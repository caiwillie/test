/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.rest.dto.incidents;

public class IncidentFlowNodeDto {
    private String id;
    private int count;

    public IncidentFlowNodeDto() {
    }

    public IncidentFlowNodeDto(String flowNodeId, int count) {
        this.id = flowNodeId;
        this.count = count;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IncidentFlowNodeDto that = (IncidentFlowNodeDto)o;
        if (this.count == that.count) return this.id != null ? this.id.equals(that.id) : that.id == null;
        return false;
    }

    public int hashCode() {
        int result = this.id != null ? this.id.hashCode() : 0;
        result = 31 * result + this.count;
        return result;
    }
}
