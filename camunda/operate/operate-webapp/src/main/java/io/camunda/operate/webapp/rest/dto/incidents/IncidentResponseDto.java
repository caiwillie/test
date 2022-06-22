/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentErrorTypeDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentFlowNodeDto
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.webapp.rest.dto.incidents.IncidentDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentErrorTypeDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentFlowNodeDto;
import java.util.ArrayList;
import java.util.List;

public class IncidentResponseDto {
    private long count;
    private List<IncidentDto> incidents = new ArrayList<IncidentDto>();
    private List<IncidentErrorTypeDto> errorTypes = new ArrayList<IncidentErrorTypeDto>();
    private List<IncidentFlowNodeDto> flowNodes = new ArrayList<IncidentFlowNodeDto>();

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<IncidentDto> getIncidents() {
        return this.incidents;
    }

    public void setIncidents(List<IncidentDto> incidents) {
        this.incidents = incidents;
    }

    public List<IncidentErrorTypeDto> getErrorTypes() {
        return this.errorTypes;
    }

    public void setErrorTypes(List<IncidentErrorTypeDto> errorTypes) {
        this.errorTypes = errorTypes;
    }

    public List<IncidentFlowNodeDto> getFlowNodes() {
        return this.flowNodes;
    }

    public void setFlowNodes(List<IncidentFlowNodeDto> flowNodes) {
        this.flowNodes = flowNodes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IncidentResponseDto that = (IncidentResponseDto)o;
        if (this.count != that.count) {
            return false;
        }
        if (this.incidents != null ? !this.incidents.equals(that.incidents) : that.incidents != null) {
            return false;
        }
        if (!(this.errorTypes != null ? !this.errorTypes.equals(that.errorTypes) : that.errorTypes != null)) return this.flowNodes != null ? this.flowNodes.equals(that.flowNodes) : that.flowNodes == null;
        return false;
    }

    public int hashCode() {
        int result = (int)(this.count ^ this.count >>> 32);
        result = 31 * result + (this.incidents != null ? this.incidents.hashCode() : 0);
        result = 31 * result + (this.errorTypes != null ? this.errorTypes.hashCode() : 0);
        result = 31 * result + (this.flowNodes != null ? this.flowNodes.hashCode() : 0);
        return result;
    }
}
