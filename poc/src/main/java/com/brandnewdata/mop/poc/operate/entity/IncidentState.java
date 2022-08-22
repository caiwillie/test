package com.brandnewdata.mop.poc.operate.entity;

public enum IncidentState {
    ACTIVE,
    RESOLVED;

    public static IncidentState fromZeebeIncidentIntent(String zeebeIncidentIntent) {
        switch (zeebeIncidentIntent) {
            case "CREATED":
            case "RESOLVE_FAILED":
                return ACTIVE;
            case "RESOLVED":
                return RESOLVED;
            default:
                return ACTIVE;
        }
    }
}
