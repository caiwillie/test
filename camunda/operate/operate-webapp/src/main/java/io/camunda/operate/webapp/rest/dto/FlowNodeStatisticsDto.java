/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.rest.dto;

public class FlowNodeStatisticsDto {
    private String activityId;
    private Long active = 0L;
    private Long canceled = 0L;
    private Long incidents = 0L;
    private Long completed = 0L;

    public FlowNodeStatisticsDto() {
    }

    public FlowNodeStatisticsDto(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityId() {
        return this.activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Long getActive() {
        return this.active;
    }

    public void setActive(Long active) {
        this.active = active;
    }

    public void addActive(Long active) {
        this.active = this.active + active;
    }

    public Long getCanceled() {
        return this.canceled;
    }

    public void setCanceled(Long canceled) {
        this.canceled = canceled;
    }

    public void addCanceled(Long canceled) {
        this.canceled = this.canceled + canceled;
    }

    public Long getIncidents() {
        return this.incidents;
    }

    public void setIncidents(Long incidents) {
        this.incidents = incidents;
    }

    public void addIncidents(Long incidents) {
        this.incidents = this.incidents + incidents;
    }

    public Long getCompleted() {
        return this.completed;
    }

    public void setCompleted(Long completed) {
        this.completed = completed;
    }

    public void addCompleted(Long completed) {
        this.completed = this.completed + completed;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        FlowNodeStatisticsDto that = (FlowNodeStatisticsDto)o;
        if (this.activityId != null ? !this.activityId.equals(that.activityId) : that.activityId != null) {
            return false;
        }
        if (this.active != null ? !this.active.equals(that.active) : that.active != null) {
            return false;
        }
        if (this.canceled != null ? !this.canceled.equals(that.canceled) : that.canceled != null) {
            return false;
        }
        if (!(this.incidents != null ? !this.incidents.equals(that.incidents) : that.incidents != null)) return this.completed != null ? this.completed.equals(that.completed) : that.completed == null;
        return false;
    }

    public int hashCode() {
        int result = this.activityId != null ? this.activityId.hashCode() : 0;
        result = 31 * result + (this.active != null ? this.active.hashCode() : 0);
        result = 31 * result + (this.canceled != null ? this.canceled.hashCode() : 0);
        result = 31 * result + (this.incidents != null ? this.incidents.hashCode() : 0);
        result = 31 * result + (this.completed != null ? this.completed.hashCode() : 0);
        return result;
    }
}
