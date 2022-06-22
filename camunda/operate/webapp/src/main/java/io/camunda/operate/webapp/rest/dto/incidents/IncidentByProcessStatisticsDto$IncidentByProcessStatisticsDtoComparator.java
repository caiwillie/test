/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.webapp.rest.dto.incidents.IncidentByProcessStatisticsDto;
import java.util.Comparator;
/*

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
*/
