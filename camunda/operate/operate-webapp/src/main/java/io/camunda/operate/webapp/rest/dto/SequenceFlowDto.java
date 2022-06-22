/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.SequenceFlowEntity
 *  io.camunda.operate.util.ConversionUtils
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.SequenceFlowEntity;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;

public class SequenceFlowDto
implements CreatableFromEntity<SequenceFlowDto, SequenceFlowEntity> {
    private String processInstanceId;
    private String activityId;

    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    public SequenceFlowDto setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public String getActivityId() {
        return this.activityId;
    }

    public SequenceFlowDto setActivityId(String activityId) {
        this.activityId = activityId;
        return this;
    }

    public SequenceFlowDto fillFrom(SequenceFlowEntity entity) {
        return this.setProcessInstanceId(ConversionUtils.toStringOrNull((Object)entity.getProcessInstanceKey())).setActivityId(entity.getActivityId());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        SequenceFlowDto that = (SequenceFlowDto)o;
        if (!(this.processInstanceId != null ? !this.processInstanceId.equals(that.processInstanceId) : that.processInstanceId != null)) return this.activityId != null ? this.activityId.equals(that.activityId) : that.activityId == null;
        return false;
    }

    public int hashCode() {
        int result = this.processInstanceId != null ? this.processInstanceId.hashCode() : 0;
        result = 31 * result + (this.activityId != null ? this.activityId.hashCode() : 0);
        return result;
    }
}
