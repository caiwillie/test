/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.BatchOperationEntity
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 *  io.camunda.operate.webapp.rest.dto.operation.OperationTypeDto
 */
package io.camunda.operate.webapp.rest.dto.operation;

import io.camunda.operate.entities.BatchOperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import io.camunda.operate.webapp.rest.dto.operation.OperationTypeDto;
import java.time.OffsetDateTime;
import java.util.Arrays;

public class BatchOperationDto
implements CreatableFromEntity<BatchOperationDto, BatchOperationEntity> {
    private String id;
    private String name;
    private OperationTypeDto type;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Integer instancesCount = 0;
    private Integer operationsTotalCount = 0;
    private Integer operationsFinishedCount = 0;
    private String[] sortValues;

    public String getName() {
        return this.name;
    }

    public BatchOperationDto setName(String name) {
        this.name = name;
        return this;
    }

    public OperationTypeDto getType() {
        return this.type;
    }

    public BatchOperationDto setType(OperationTypeDto type) {
        this.type = type;
        return this;
    }

    public OffsetDateTime getStartDate() {
        return this.startDate;
    }

    public BatchOperationDto setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public OffsetDateTime getEndDate() {
        return this.endDate;
    }

    public BatchOperationDto setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public Integer getInstancesCount() {
        return this.instancesCount;
    }

    public BatchOperationDto setInstancesCount(Integer instancesCount) {
        this.instancesCount = instancesCount;
        return this;
    }

    public Integer getOperationsTotalCount() {
        return this.operationsTotalCount;
    }

    public BatchOperationDto setOperationsTotalCount(Integer operationsTotalCount) {
        this.operationsTotalCount = operationsTotalCount;
        return this;
    }

    public Integer getOperationsFinishedCount() {
        return this.operationsFinishedCount;
    }

    public BatchOperationDto setOperationsFinishedCount(Integer operationsFinishedCount) {
        this.operationsFinishedCount = operationsFinishedCount;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public BatchOperationDto setId(String id) {
        this.id = id;
        return this;
    }

    public String[] getSortValues() {
        return this.sortValues;
    }

    public BatchOperationDto setSortValues(String[] sortValues) {
        this.sortValues = sortValues;
        return this;
    }

    public BatchOperationDto fillFrom(BatchOperationEntity batchOperationEntity) {
        return this.setId(batchOperationEntity.getId()).setName(batchOperationEntity.getName()).setType(OperationTypeDto.getType((OperationType)batchOperationEntity.getType())).setStartDate(batchOperationEntity.getStartDate()).setEndDate(batchOperationEntity.getEndDate()).setInstancesCount(batchOperationEntity.getInstancesCount()).setOperationsTotalCount(batchOperationEntity.getOperationsTotalCount()).setOperationsFinishedCount(batchOperationEntity.getOperationsFinishedCount()).setSortValues((String[])Arrays.stream(batchOperationEntity.getSortValues()).map(String::valueOf).toArray(String[]::new));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        BatchOperationDto that = (BatchOperationDto)o;
        if (this.id != null ? !this.id.equals(that.id) : that.id != null) {
            return false;
        }
        if (this.name != null ? !this.name.equals(that.name) : that.name != null) {
            return false;
        }
        if (this.type != that.type) {
            return false;
        }
        if (this.startDate != null ? !this.startDate.equals(that.startDate) : that.startDate != null) {
            return false;
        }
        if (this.endDate != null ? !this.endDate.equals(that.endDate) : that.endDate != null) {
            return false;
        }
        if (this.instancesCount != null ? !this.instancesCount.equals(that.instancesCount) : that.instancesCount != null) {
            return false;
        }
        if (this.operationsTotalCount != null ? !this.operationsTotalCount.equals(that.operationsTotalCount) : that.operationsTotalCount != null) {
            return false;
        }
        if (this.operationsFinishedCount != null) {
            if (this.operationsFinishedCount.equals(that.operationsFinishedCount)) return Arrays.equals(this.sortValues, that.sortValues);
        } else if (that.operationsFinishedCount == null) return Arrays.equals(this.sortValues, that.sortValues);
        return false;
    }

    public int hashCode() {
        int result = this.id != null ? this.id.hashCode() : 0;
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        result = 31 * result + (this.startDate != null ? this.startDate.hashCode() : 0);
        result = 31 * result + (this.endDate != null ? this.endDate.hashCode() : 0);
        result = 31 * result + (this.instancesCount != null ? this.instancesCount.hashCode() : 0);
        result = 31 * result + (this.operationsTotalCount != null ? this.operationsTotalCount.hashCode() : 0);
        result = 31 * result + (this.operationsFinishedCount != null ? this.operationsFinishedCount.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(this.sortValues);
        return result;
    }
}
