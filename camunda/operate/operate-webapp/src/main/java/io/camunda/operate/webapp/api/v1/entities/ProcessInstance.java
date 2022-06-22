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
public class ProcessInstance {
    public static final String KEY = "processInstanceKey";
    public static final String VERSION = "processVersion";
    public static final String BPMN_PROCESS_ID = "bpmnProcessId";
    public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    public static final String PARENT_KEY = "parentProcessInstanceKey";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String STATE = "state";
    private Long key;
    private Integer processVersion;
    private String bpmnProcessId;
    private Long parentKey;
    private String startDate;
    private String endDate;
    private String state;
    private Long processDefinitionKey;

    public Long getKey() {
        return this.key;
    }

    public ProcessInstance setKey(long key) {
        this.key = key;
        return this;
    }

    public Integer getProcessVersion() {
        return this.processVersion;
    }

    public ProcessInstance setProcessVersion(int processVersion) {
        this.processVersion = processVersion;
        return this;
    }

    public String getBpmnProcessId() {
        return this.bpmnProcessId;
    }

    public ProcessInstance setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
        return this;
    }

    public Long getParentKey() {
        return this.parentKey;
    }

    public ProcessInstance setParentKey(long parentKey) {
        this.parentKey = parentKey;
        return this;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public ProcessInstance setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public ProcessInstance setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getState() {
        return this.state;
    }

    public ProcessInstance setState(String state) {
        this.state = state;
        return this;
    }

    public Long getProcessDefinitionKey() {
        return this.processDefinitionKey;
    }

    public ProcessInstance setProcessDefinitionKey(long processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
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
        ProcessInstance that = (ProcessInstance)o;
        return Objects.equals(this.key, that.key) && Objects.equals(this.processVersion, that.processVersion) && Objects.equals(this.bpmnProcessId, that.bpmnProcessId) && Objects.equals(this.parentKey, that.parentKey) && Objects.equals(this.startDate, that.startDate) && Objects.equals(this.endDate, that.endDate) && Objects.equals(this.state, that.state) && Objects.equals(this.processDefinitionKey, that.processDefinitionKey);
    }

    public int hashCode() {
        return Objects.hash(this.key, this.processVersion, this.bpmnProcessId, this.parentKey, this.startDate, this.endDate, this.state, this.processDefinitionKey);
    }

    public String toString() {
        return "ProcessInstance{key=" + this.key + ", processVersion=" + this.processVersion + ", bpmnProcessId='" + this.bpmnProcessId + "', parentKey=" + this.parentKey + ", startDate=" + this.startDate + ", endDate=" + this.endDate + ", state='" + this.state + "', processDefinitionKey=" + this.processDefinitionKey + "}";
    }
}
