/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 */
package io.camunda.operate.webapp.api.v1.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.Objects;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class Incident {
    public static final String KEY = "key";
    public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    public static final String PROCESS_INSTANCE_KEY = "processInstanceKey";
    public static final String TYPE = "errorType";
    public static final String MESSAGE = "errorMessage";
    public static final String CREATION_TIME = "creationTime";
    public static final String STATE = "state";
    public static final String MESSAGE_FIELD = "message";
    public static final String TYPE_FIELD = "type";
    public static final Map<String, String> OBJECT_TO_ELASTICSEARCH = Map.of("type", "errorType", "message", "errorMessage");
    private Long key;
    private Long processDefinitionKey;
    private Long processInstanceKey;
    private String type;
    private String message;
    private String creationTime;
    private String state;

    public Long getKey() {
        return this.key;
    }

    public Incident setKey(Long key) {
        this.key = key;
        return this;
    }

    public Long getProcessDefinitionKey() {
        return this.processDefinitionKey;
    }

    public Incident setProcessDefinitionKey(Long processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
        return this;
    }

    public Long getProcessInstanceKey() {
        return this.processInstanceKey;
    }

    public Incident setProcessInstanceKey(Long processInstanceKey) {
        this.processInstanceKey = processInstanceKey;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public Incident setType(String type) {
        this.type = type;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public Incident setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getCreationTime() {
        return this.creationTime;
    }

    public Incident setCreationTime(String creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getState() {
        return this.state;
    }

    public Incident setState(String state) {
        this.state = state;
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
        Incident incident = (Incident)o;
        return Objects.equals(this.key, incident.key) && Objects.equals(this.processDefinitionKey, incident.processDefinitionKey) && Objects.equals(this.processInstanceKey, incident.processInstanceKey) && Objects.equals(this.type, incident.type) && Objects.equals(this.message, incident.message) && Objects.equals(this.creationTime, incident.creationTime) && Objects.equals(this.state, incident.state);
    }

    public int hashCode() {
        return Objects.hash(this.key, this.processDefinitionKey, this.processInstanceKey, this.type, this.message, this.creationTime, this.state);
    }

    public String toString() {
        return "Incident{key=" + this.key + ", processDefinitionKey=" + this.processDefinitionKey + ", processInstanceKey=" + this.processInstanceKey + ", type='" + this.type + "', message='" + this.message + "', creationTime=" + this.creationTime + ", state='" + this.state + "'}";
    }
}
