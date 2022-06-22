/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.ProcessEntity
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.ProcessEntity;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Process object")
public class ProcessDto
implements CreatableFromEntity<ProcessDto, ProcessEntity> {
    @ApiModelProperty(value="Unique id of the process, must be used when filtering instances by process ids.")
    private String id;
    private String name;
    private int version;
    private String bpmnProcessId;

    public String getId() {
        return this.id;
    }

    public ProcessDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public ProcessDto setName(String name) {
        this.name = name;
        return this;
    }

    public int getVersion() {
        return this.version;
    }

    public ProcessDto setVersion(int version) {
        this.version = version;
        return this;
    }

    public String getBpmnProcessId() {
        return this.bpmnProcessId;
    }

    public ProcessDto setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
        return this;
    }

    public ProcessDto fillFrom(ProcessEntity processEntity) {
        this.setId(processEntity.getId()).setBpmnProcessId(processEntity.getBpmnProcessId()).setName(processEntity.getName()).setVersion(processEntity.getVersion());
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
        ProcessDto that = (ProcessDto)o;
        if (this.version != that.version) {
            return false;
        }
        if (this.id != null ? !this.id.equals(that.id) : that.id != null) {
            return false;
        }
        if (!(this.name != null ? !this.name.equals(that.name) : that.name != null)) return this.bpmnProcessId != null ? this.bpmnProcessId.equals(that.bpmnProcessId) : that.bpmnProcessId == null;
        return false;
    }

    public int hashCode() {
        int result = this.id != null ? this.id.hashCode() : 0;
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        result = 31 * result + this.version;
        result = 31 * result + (this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0);
        return result;
    }
}
