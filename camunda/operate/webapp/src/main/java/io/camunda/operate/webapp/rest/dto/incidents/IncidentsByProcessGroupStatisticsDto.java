/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.annotation.JsonDeserialize
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentsByProcessGroupStatisticsDto$IncidentsByProcessGroupStatisticsDtoComparator
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentsByProcessGroupStatisticsDto;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class IncidentsByProcessGroupStatisticsDto {
    public static final Comparator<IncidentsByProcessGroupStatisticsDto> COMPARATOR = new IncidentsByProcessGroupStatisticsDtoComparator();
    private String bpmnProcessId;
    private String processName;
    private long instancesWithActiveIncidentsCount;
    private long activeInstancesCount;
    @JsonDeserialize(as=TreeSet.class)
    private Set<IncidentByProcessStatisticsDto> processes = new TreeSet<IncidentByProcessStatisticsDto>(IncidentByProcessStatisticsDto.COMPARATOR);

    public String getBpmnProcessId() {
        return this.bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }

    public String getProcessName() {
        return this.processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public long getInstancesWithActiveIncidentsCount() {
        return this.instancesWithActiveIncidentsCount;
    }

    public void setInstancesWithActiveIncidentsCount(long instancesWithActiveIncidentsCount) {
        this.instancesWithActiveIncidentsCount = instancesWithActiveIncidentsCount;
    }

    public long getActiveInstancesCount() {
        return this.activeInstancesCount;
    }

    public void setActiveInstancesCount(long activeInstancesCount) {
        this.activeInstancesCount = activeInstancesCount;
    }

    public Set<IncidentByProcessStatisticsDto> getProcesses() {
        return this.processes;
    }

    public void setProcesses(Set<IncidentByProcessStatisticsDto> processes) {
        this.processes = processes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IncidentsByProcessGroupStatisticsDto that = (IncidentsByProcessGroupStatisticsDto)o;
        if (this.instancesWithActiveIncidentsCount != that.instancesWithActiveIncidentsCount) {
            return false;
        }
        if (this.activeInstancesCount != that.activeInstancesCount) {
            return false;
        }
        if (this.bpmnProcessId != null ? !this.bpmnProcessId.equals(that.bpmnProcessId) : that.bpmnProcessId != null) {
            return false;
        }
        if (!(this.processName != null ? !this.processName.equals(that.processName) : that.processName != null)) return this.processes != null ? this.processes.equals(that.processes) : that.processes == null;
        return false;
    }

    public int hashCode() {
        int result = this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0;
        result = 31 * result + (this.processName != null ? this.processName.hashCode() : 0);
        result = 31 * result + (int)(this.instancesWithActiveIncidentsCount ^ this.instancesWithActiveIncidentsCount >>> 32);
        result = 31 * result + (int)(this.activeInstancesCount ^ this.activeInstancesCount >>> 32);
        result = 31 * result + (this.processes != null ? this.processes.hashCode() : 0);
        return result;
    }

    public static class IncidentsByProcessGroupStatisticsDtoComparator implements Comparator<IncidentsByProcessGroupStatisticsDto> {
        @Override
        public int compare(IncidentsByProcessGroupStatisticsDto o1, IncidentsByProcessGroupStatisticsDto o2) {
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
            int result = Long.compare(o2.getInstancesWithActiveIncidentsCount(), o1.getInstancesWithActiveIncidentsCount());
            if (result != 0) return result;
            result = Long.compare(o2.getActiveInstancesCount(), o1.getActiveInstancesCount());
            if (result != 0) return result;
            result = o1.getBpmnProcessId().compareTo(o2.getBpmnProcessId());
            return result;
        }
    }
}
