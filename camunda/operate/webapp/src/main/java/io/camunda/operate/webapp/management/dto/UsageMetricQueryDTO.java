/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.format.annotation.DateTimeFormat
 */
package io.camunda.operate.webapp.management.dto;

import java.time.OffsetDateTime;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

public class UsageMetricQueryDTO {
    private static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;
    @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    private OffsetDateTime startTime;
    @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    private OffsetDateTime endTime;
    private int pageSize = Integer.MAX_VALUE;

    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UsageMetricQueryDTO)) {
            return false;
        }
        UsageMetricQueryDTO that = (UsageMetricQueryDTO)o;
        return this.pageSize == that.pageSize && Objects.equals(this.startTime, that.startTime) && Objects.equals(this.endTime, that.endTime);
    }

    public int hashCode() {
        return Objects.hash(this.startTime, this.endTime, this.pageSize);
    }
}
