/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 */
package io.camunda.operate.webapp.api.v1.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class FlowNodeInstance {
    public static final String KEY = "key";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String INCIDENT_KEY = "incidentKey";
    public static final String TYPE = "type";
    public static final String STATE = "state";
    public static final String INCIDENT = "incident";
    public static final String PROCESS_INSTANCE_KEY = "processInstanceKey";
    private Long key;
    private Long processInstanceKey;
    private String startDate;
    private String endDate;
    private Long incidentKey;
    private String type;
    private String state;
    private Boolean incident;

    public Long getProcessInstanceKey() {
        return this.processInstanceKey;
    }

    public FlowNodeInstance setProcessInstanceKey(Long processInstanceKey) {
        this.processInstanceKey = processInstanceKey;
        return this;
    }

    public Long getKey() {
        return this.key;
    }

    public FlowNodeInstance setKey(Long key) {
        this.key = key;
        return this;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public FlowNodeInstance setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public FlowNodeInstance setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public Long getIncidentKey() {
        return this.incidentKey;
    }

    public FlowNodeInstance setIncidentKey(Long incidentKey) {
        this.incidentKey = incidentKey;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public FlowNodeInstance setType(String type) {
        this.type = type;
        return this;
    }

    public String getState() {
        return this.state;
    }

    public FlowNodeInstance setState(String state) {
        this.state = state;
        return this;
    }

    public Boolean getIncident() {
        return this.incident;
    }

    public FlowNodeInstance setIncident(Boolean incident) {
        this.incident = incident;
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
        FlowNodeInstance that = (FlowNodeInstance)o;
        return Objects.equals(this.key, that.key) && Objects.equals(this.processInstanceKey, that.processInstanceKey) && Objects.equals(this.startDate, that.startDate) && Objects.equals(this.endDate, that.endDate) && Objects.equals(this.incidentKey, that.incidentKey) && Objects.equals(this.type, that.type) && Objects.equals(this.state, that.state) && Objects.equals(this.incident, that.incident);
    }

    public int hashCode() {
        return Objects.hash(this.key, this.processInstanceKey, this.startDate, this.endDate, this.incidentKey, this.type, this.state, this.incident);
    }

    public String toString() {
        return "FlowNodeInstance{key=" + this.key + ", processInstanceKey=" + this.processInstanceKey + ", startDate='" + this.startDate + "', endDate='" + this.endDate + "', incidentKey=" + this.incidentKey + ", type='" + this.type + "', state='" + this.state + "', incident=" + this.incident + "}";
    }
}
