/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto
 */
package io.camunda.operate.webapp.rest.dto.listview;

import io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto;
import java.util.ArrayList;
import java.util.List;

public class ListViewResponseDto {
    private List<ListViewProcessInstanceDto> processInstances = new ArrayList<ListViewProcessInstanceDto>();
    private long totalCount;

    public List<ListViewProcessInstanceDto> getProcessInstances() {
        return this.processInstances;
    }

    public void setProcessInstances(List<ListViewProcessInstanceDto> processInstances) {
        this.processInstances = processInstances;
    }

    public long getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        ListViewResponseDto that = (ListViewResponseDto)o;
        if (this.totalCount == that.totalCount) return this.processInstances != null ? this.processInstances.equals(that.processInstances) : that.processInstances == null;
        return false;
    }

    public int hashCode() {
        int result = this.processInstances != null ? this.processInstances.hashCode() : 0;
        result = 31 * result + (int)(this.totalCount ^ this.totalCount >>> 32);
        return result;
    }
}
