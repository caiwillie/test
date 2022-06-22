/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.rest.dto;

public class ProcessInstanceCoreStatisticsDto {
    private Long running = 0L;
    private Long active = 0L;
    private Long withIncidents = 0L;

    public Long getRunning() {
        return this.running;
    }

    public ProcessInstanceCoreStatisticsDto setRunning(Long running) {
        this.running = running;
        return this;
    }

    public Long getActive() {
        return this.active;
    }

    public ProcessInstanceCoreStatisticsDto setActive(Long active) {
        this.active = active;
        return this;
    }

    public Long getWithIncidents() {
        return this.withIncidents;
    }

    public ProcessInstanceCoreStatisticsDto setWithIncidents(Long withIncidents) {
        this.withIncidents = withIncidents;
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
        ProcessInstanceCoreStatisticsDto that = (ProcessInstanceCoreStatisticsDto)o;
        if (this.running != null ? !this.running.equals(that.running) : that.running != null) {
            return false;
        }
        if (!(this.active != null ? !this.active.equals(that.active) : that.active != null)) return this.withIncidents != null ? this.withIncidents.equals(that.withIncidents) : that.withIncidents == null;
        return false;
    }

    public int hashCode() {
        int result = this.running != null ? this.running.hashCode() : 0;
        result = 31 * result + (this.active != null ? this.active.hashCode() : 0);
        result = 31 * result + (this.withIncidents != null ? this.withIncidents.hashCode() : 0);
        return result;
    }
}
