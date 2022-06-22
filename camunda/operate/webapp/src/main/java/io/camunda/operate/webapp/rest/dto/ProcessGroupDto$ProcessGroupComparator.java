/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.ProcessGroupDto
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.webapp.rest.dto.ProcessGroupDto;
import java.util.Comparator;
/*

public static class ProcessGroupComparator implements Comparator<ProcessGroupDto> {
    @Override
    public int compare(ProcessGroupDto o1, ProcessGroupDto o2) {
        if (o1.getName() == null && o2.getName() == null) {
            return o1.getBpmnProcessId().compareTo(o2.getBpmnProcessId());
        }
        if (o1.getName() == null) {
            return 1;
        }
        if (o2.getName() == null) {
            return -1;
        }
        if (o1.getName().equals(o2.getName())) return o1.getBpmnProcessId().compareTo(o2.getBpmnProcessId());
        return o1.getName().compareTo(o2.getName());
    }
}
*/
