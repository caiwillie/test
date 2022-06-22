/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentsByErrorMsgStatisticsDto
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.webapp.rest.dto.incidents.IncidentsByErrorMsgStatisticsDto;
import java.util.Comparator;
/*

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
*/
