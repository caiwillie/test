/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto$IncidentByProcessStatisticsDtoComparator
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto;
import java.util.Comparator;

public class IncidentByProcessStatisticsDto
implements Comparable<IncidentByProcessStatisticsDto> {
    public static final Comparator<IncidentByProcessStatisticsDto> COMPARATOR = new IncidentByProcessStatisticsDtoComparator();
    private String processId;
    private int version;
    private String name;
    private String bpmnProcessId;
    private String errorMessage;
    private long instancesWithActiveIncidentsCount;
    private long activeInstancesCount;

    public IncidentByProcessStatisticsDto() {
    }

    public IncidentByProcessStatisticsDto(String processId, long instancesWithActiveIncidentsCount, long activeInstancesCount) {
        this.processId = processId;
        this.instancesWithActiveIncidentsCount = instancesWithActiveIncidentsCount;
        this.activeInstancesCount = activeInstancesCount;
    }

    public IncidentByProcessStatisticsDto(String processId, String errorMessage, long instancesWithActiveIncidentsCount) {
        this.processId = processId;
        this.errorMessage = errorMessage;
        this.instancesWithActiveIncidentsCount = instancesWithActiveIncidentsCount;
    }

    public String getProcessId() {
        return this.processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBpmnProcessId() {
        return this.bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IncidentByProcessStatisticsDto that = (IncidentByProcessStatisticsDto)o;
        if (this.version != that.version) {
            return false;
        }
        if (this.instancesWithActiveIncidentsCount != that.instancesWithActiveIncidentsCount) {
            return false;
        }
        if (this.processId != null ? !this.processId.equals(that.processId) : that.processId != null) {
            return false;
        }
        if (this.name != null ? !this.name.equals(that.name) : that.name != null) {
            return false;
        }
        if (this.bpmnProcessId != null ? !this.bpmnProcessId.equals(that.bpmnProcessId) : that.bpmnProcessId != null) {
            return false;
        }
        if (!(this.errorMessage != null ? !this.errorMessage.equals(that.errorMessage) : that.errorMessage != null)) return this.activeInstancesCount == that.activeInstancesCount;
        return false;
    }

    public int hashCode() {
        int result = this.processId != null ? this.processId.hashCode() : 0;
        result = 31 * result + this.version;
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        result = 31 * result + (this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0);
        result = 31 * result + (this.errorMessage != null ? this.errorMessage.hashCode() : 0);
        result = 31 * result + (int)(this.instancesWithActiveIncidentsCount ^ this.instancesWithActiveIncidentsCount >>> 32);
        result = 31 * result + (int)this.activeInstancesCount;
        return result;
    }

    @Override
    public int compareTo(IncidentByProcessStatisticsDto o) {
        return COMPARATOR.compare(this, o);
    }

    public static class IncidentByProcessStatisticsDtoComparator implements Comparator<IncidentByProcessStatisticsDto> {
        @Override
        public int compare(IncidentByProcessStatisticsDto o1, IncidentByProcessStatisticsDto o2) {
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
            result = this.emptyStringWhenNull(o1.getBpmnProcessId()).compareTo(this.emptyStringWhenNull(o2.getBpmnProcessId()));
            if (result != 0) return result;
            result = Integer.compare(o1.getVersion(), o2.getVersion());
            return result;
        }

        private String emptyStringWhenNull(String aString) {
            return aString == null ? "" : aString;
        }
    }
}
