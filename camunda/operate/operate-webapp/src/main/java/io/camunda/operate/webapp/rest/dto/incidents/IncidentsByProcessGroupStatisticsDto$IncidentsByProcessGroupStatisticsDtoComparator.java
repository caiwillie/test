/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentsByProcessGroupStatisticsDto
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.webapp.rest.dto.incidents.IncidentsByProcessGroupStatisticsDto;
import java.util.Comparator;
/*
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
*/
