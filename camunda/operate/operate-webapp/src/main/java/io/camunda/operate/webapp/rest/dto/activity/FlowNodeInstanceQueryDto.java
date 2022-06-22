/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModelProperty
 */
package io.camunda.operate.webapp.rest.dto.activity;

import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.Objects;

public class FlowNodeInstanceQueryDto {
    private String processInstanceId;
    private String treePath;
    private Object[] searchBefore;
    private Object[] searchBeforeOrEqual;
    private Object[] searchAfter;
    private Object[] searchAfterOrEqual;
    private Integer pageSize;

    public FlowNodeInstanceQueryDto() {
    }

    public FlowNodeInstanceQueryDto(String processInstanceId, String treePath) {
        this.processInstanceId = processInstanceId;
        this.treePath = treePath;
    }

    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    public FlowNodeInstanceQueryDto setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public String getTreePath() {
        return this.treePath;
    }

    public FlowNodeInstanceQueryDto setTreePath(String treePath) {
        this.treePath = treePath;
        return this;
    }

    @ApiModelProperty(value="Array of two strings: copy/paste of sortValues field from one of the operations.", example="[\"9223372036854775807\", \"1583836503404\"]")
    public Object[] getSearchBefore() {
        return this.searchBefore;
    }

    public FlowNodeInstanceQueryDto setSearchBefore(Object[] searchBefore) {
        this.searchBefore = searchBefore;
        return this;
    }

    public Object[] getSearchBeforeOrEqual() {
        return this.searchBeforeOrEqual;
    }

    public FlowNodeInstanceQueryDto setSearchBeforeOrEqual(Object[] searchBeforeOrEqual) {
        this.searchBeforeOrEqual = searchBeforeOrEqual;
        return this;
    }

    @ApiModelProperty(value="Array of two strings: copy/paste of sortValues field from one of the operations.", example="[\"1583836151645\", \"1583836128180\"]")
    public Object[] getSearchAfter() {
        return this.searchAfter;
    }

    public FlowNodeInstanceQueryDto setSearchAfter(Object[] searchAfter) {
        this.searchAfter = searchAfter;
        return this;
    }

    public Object[] getSearchAfterOrEqual() {
        return this.searchAfterOrEqual;
    }

    public FlowNodeInstanceQueryDto setSearchAfterOrEqual(Object[] searchAfterOrEqual) {
        this.searchAfterOrEqual = searchAfterOrEqual;
        return this;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public FlowNodeInstanceQueryDto setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public FlowNodeInstanceQueryDto createCopy() {
        return new FlowNodeInstanceQueryDto().setSearchBefore(this.searchBefore).setSearchAfter(this.searchAfter).setPageSize(this.pageSize).setSearchAfterOrEqual(this.searchAfterOrEqual).setSearchBeforeOrEqual(this.searchBeforeOrEqual).setTreePath(this.treePath).setProcessInstanceId(this.processInstanceId);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        FlowNodeInstanceQueryDto queryDto = (FlowNodeInstanceQueryDto)o;
        return Objects.equals(this.processInstanceId, queryDto.processInstanceId) && Objects.equals(this.treePath, queryDto.treePath) && Arrays.equals(this.searchBefore, queryDto.searchBefore) && Arrays.equals(this.searchBeforeOrEqual, queryDto.searchBeforeOrEqual) && Arrays.equals(this.searchAfter, queryDto.searchAfter) && Arrays.equals(this.searchAfterOrEqual, queryDto.searchAfterOrEqual) && Objects.equals(this.pageSize, queryDto.pageSize);
    }

    public int hashCode() {
        int result = Objects.hash(this.processInstanceId, this.treePath, this.pageSize);
        result = 31 * result + Arrays.hashCode(this.searchBefore);
        result = 31 * result + Arrays.hashCode(this.searchBeforeOrEqual);
        result = 31 * result + Arrays.hashCode(this.searchAfter);
        result = 31 * result + Arrays.hashCode(this.searchAfterOrEqual);
        return result;
    }
}
