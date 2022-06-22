/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.annotation.JsonDeserialize
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentsByErrorMsgStatisticsDto$IncidentsByErrorMsgStatisticsDtoComparator
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentsByErrorMsgStatisticsDto;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class IncidentsByErrorMsgStatisticsDto {
    public static final Comparator<IncidentsByErrorMsgStatisticsDto> COMPARATOR = new IncidentsByErrorMsgStatisticsDtoComparator();
    private String errorMessage;
    private long instancesWithErrorCount;
    @JsonDeserialize(as=TreeSet.class)
    private Set<IncidentByProcessStatisticsDto> processes = new TreeSet<IncidentByProcessStatisticsDto>();

    public IncidentsByErrorMsgStatisticsDto() {
    }

    public IncidentsByErrorMsgStatisticsDto(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getInstancesWithErrorCount() {
        return this.instancesWithErrorCount;
    }

    public void setInstancesWithErrorCount(long instancesWithErrorCount) {
        this.instancesWithErrorCount = instancesWithErrorCount;
    }

    public Set<IncidentByProcessStatisticsDto> getProcesses() {
        return this.processes;
    }

    public void setProcesses(Set<IncidentByProcessStatisticsDto> processes) {
        this.processes = processes;
    }

    public void recordInstancesCount(long count) {
        this.instancesWithErrorCount += count;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IncidentsByErrorMsgStatisticsDto that = (IncidentsByErrorMsgStatisticsDto)o;
        if (this.instancesWithErrorCount != that.instancesWithErrorCount) {
            return false;
        }
        if (!(this.errorMessage != null ? !this.errorMessage.equals(that.errorMessage) : that.errorMessage != null)) return this.processes != null ? this.processes.equals(that.processes) : that.processes == null;
        return false;
    }

    public int hashCode() {
        int result = this.errorMessage != null ? this.errorMessage.hashCode() : 0;
        result = 31 * result + (int)(this.instancesWithErrorCount ^ this.instancesWithErrorCount >>> 32);
        result = 31 * result + (this.processes != null ? this.processes.hashCode() : 0);
        return result;
    }

    public static class IncidentsByErrorMsgStatisticsDtoComparator implements Comparator<IncidentsByErrorMsgStatisticsDto> {
        @Override
        public int compare(IncidentsByErrorMsgStatisticsDto o1, IncidentsByErrorMsgStatisticsDto o2) {
            if (o1 == null) {
                if (o2 != null) return 1;
                return 0;
            }
            if (o2 == null) {
                return -1;
            }
            if (o1.equals((Object)o2)) {
                return 0;
            }
            int result = Long.compare(o2.getInstancesWithErrorCount(), o1.getInstancesWithErrorCount());
            if (result != 0) return result;
            result = o1.getErrorMessage().compareTo(o2.getErrorMessage());
            return result;
        }
    }
}
