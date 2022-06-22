/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.dmn.DecisionGroupDto
 */
package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.webapp.rest.dto.dmn.DecisionGroupDto;
import java.util.Comparator;
/*

public static class DecisionGroupComparator implements Comparator<DecisionGroupDto> {
    @Override
    public int compare(DecisionGroupDto o1, DecisionGroupDto o2) {
        if (o1.getName() == null && o2.getName() == null) {
            return o1.getDecisionId().compareTo(o2.getDecisionId());
        }
        if (o1.getName() == null) {
            return 1;
        }
        if (o2.getName() == null) {
            return -1;
        }
        if (o1.getName().equals(o2.getName())) return o1.getDecisionId().compareTo(o2.getDecisionId());
        return o1.getName().compareTo(o2.getName());
    }
}
*/
