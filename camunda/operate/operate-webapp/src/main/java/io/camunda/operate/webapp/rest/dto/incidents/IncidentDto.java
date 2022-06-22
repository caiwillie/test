/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.ErrorType
 *  io.camunda.operate.entities.IncidentEntity
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationState
 *  io.camunda.operate.util.ConversionUtils
 *  io.camunda.operate.webapp.es.reader.IncidentReader$IncidentDataHolder
 *  io.camunda.operate.webapp.rest.dto.DtoCreator
 *  io.camunda.operate.webapp.rest.dto.OperationDto
 *  io.camunda.operate.webapp.rest.dto.ProcessInstanceReferenceDto
 *  io.camunda.operate.webapp.rest.dto.incidents.ErrorTypeDto
 *  io.camunda.operate.webapp.rest.dto.metadata.DecisionInstanceReferenceDto
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.entities.ErrorType;
import io.camunda.operate.entities.IncidentEntity;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.webapp.es.reader.IncidentReader;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.OperationDto;
import io.camunda.operate.webapp.rest.dto.ProcessInstanceReferenceDto;
import io.camunda.operate.webapp.rest.dto.incidents.ErrorTypeDto;
import io.camunda.operate.webapp.rest.dto.metadata.DecisionInstanceReferenceDto;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class IncidentDto {
    public static final Comparator<IncidentDto> INCIDENT_DEFAULT_COMPARATOR = (o1, o2) -> {
        if (!o1.getErrorType().equals((Object)o2.getErrorType())) return o1.getErrorType().compareTo(o2.getErrorType());
        return o1.getId().compareTo(o2.getId());
    };
    public static final String FALLBACK_PROCESS_DEFINITION_NAME = "Unknown process";
    private String id;
    private ErrorTypeDto errorType;
    private String errorMessage;
    private String flowNodeId;
    private String flowNodeInstanceId;
    private String jobId;
    private OffsetDateTime creationTime;
    private boolean hasActiveOperation = false;
    private OperationDto lastOperation;
    private ProcessInstanceReferenceDto rootCauseInstance;
    private DecisionInstanceReferenceDto rootCauseDecision;

    public String getId() {
        return this.id;
    }

    public IncidentDto setId(String id) {
        this.id = id;
        return this;
    }

    public ErrorTypeDto getErrorType() {
        return this.errorType;
    }

    public IncidentDto setErrorType(ErrorTypeDto errorType) {
        this.errorType = errorType;
        return this;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public IncidentDto setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getFlowNodeId() {
        return this.flowNodeId;
    }

    public IncidentDto setFlowNodeId(String flowNodeId) {
        this.flowNodeId = flowNodeId;
        return this;
    }

    public String getFlowNodeInstanceId() {
        return this.flowNodeInstanceId;
    }

    public IncidentDto setFlowNodeInstanceId(String flowNodeInstanceId) {
        this.flowNodeInstanceId = flowNodeInstanceId;
        return this;
    }

    public String getJobId() {
        return this.jobId;
    }

    public IncidentDto setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public OffsetDateTime getCreationTime() {
        return this.creationTime;
    }

    public IncidentDto setCreationTime(OffsetDateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public boolean isHasActiveOperation() {
        return this.hasActiveOperation;
    }

    public IncidentDto setHasActiveOperation(boolean hasActiveOperation) {
        this.hasActiveOperation = hasActiveOperation;
        return this;
    }

    public OperationDto getLastOperation() {
        return this.lastOperation;
    }

    public IncidentDto setLastOperation(OperationDto lastOperation) {
        this.lastOperation = lastOperation;
        return this;
    }

    public ProcessInstanceReferenceDto getRootCauseInstance() {
        return this.rootCauseInstance;
    }

    public IncidentDto setRootCauseInstance(ProcessInstanceReferenceDto rootCauseInstance) {
        this.rootCauseInstance = rootCauseInstance;
        return this;
    }

    public DecisionInstanceReferenceDto getRootCauseDecision() {
        return this.rootCauseDecision;
    }

    public IncidentDto setRootCauseDecision(DecisionInstanceReferenceDto rootCauseDecision) {
        this.rootCauseDecision = rootCauseDecision;
        return this;
    }

    public static <T> IncidentDto createFrom(IncidentEntity incidentEntity, Map<Long, String> processNames, IncidentReader.IncidentDataHolder incidentData, DecisionInstanceReferenceDto rootCauseDecision) {
        return IncidentDto.createFrom(incidentEntity, Collections.<OperationEntity>emptyList(), processNames, incidentData, rootCauseDecision);
    }

    public static IncidentDto createFrom(IncidentEntity incidentEntity, List<OperationEntity> operations, Map<Long, String> processNames, IncidentReader.IncidentDataHolder incidentData, DecisionInstanceReferenceDto rootCauseDecision) {
        if (incidentEntity == null) {
            return null;
        }
        IncidentDto incident = new IncidentDto().setId(incidentEntity.getId()).setFlowNodeId(incidentEntity.getFlowNodeId()).setFlowNodeInstanceId(ConversionUtils.toStringOrNull((Object)incidentEntity.getFlowNodeInstanceKey())).setErrorMessage(incidentEntity.getErrorMessage()).setErrorType(ErrorTypeDto.createFrom((ErrorType)incidentEntity.getErrorType())).setJobId(ConversionUtils.toStringOrNull((Object)incidentEntity.getJobKey())).setCreationTime(incidentEntity.getCreationTime());
        if (operations != null && operations.size() > 0) {
            OperationEntity lastOperation = operations.get(0);
            incident.setLastOperation((OperationDto)DtoCreator.create(lastOperation, OperationDto.class)).setHasActiveOperation(operations.stream().anyMatch(o -> o.getState().equals((Object)OperationState.SCHEDULED) || o.getState().equals((Object)OperationState.LOCKED) || o.getState().equals((Object)OperationState.SENT)));
        }
        if (incidentData != null && incident.getFlowNodeInstanceId() != incidentData.getFinalFlowNodeInstanceId()) {
            incident.setFlowNodeId(incidentData.getFinalFlowNodeId());
            incident.setFlowNodeInstanceId(incidentData.getFinalFlowNodeInstanceId());
            ProcessInstanceReferenceDto rootCauseInstance = new ProcessInstanceReferenceDto().setInstanceId(String.valueOf(incidentEntity.getProcessInstanceKey())).setProcessDefinitionId(String.valueOf(incidentEntity.getProcessDefinitionKey()));
            if (processNames != null && processNames.get(incidentEntity.getProcessDefinitionKey()) != null) {
                rootCauseInstance.setProcessDefinitionName(processNames.get(incidentEntity.getProcessDefinitionKey()));
            } else {
                rootCauseInstance.setProcessDefinitionName(FALLBACK_PROCESS_DEFINITION_NAME);
            }
            incident.setRootCauseInstance(rootCauseInstance);
        }
        if (rootCauseDecision == null) return incident;
        incident.setRootCauseDecision(rootCauseDecision);
        return incident;
    }

    public static List<IncidentDto> createFrom(List<IncidentEntity> incidentEntities, Map<Long, List<OperationEntity>> operations, Map<Long, String> processNames, Map<String, IncidentReader.IncidentDataHolder> incidentData) {
        if (incidentEntities == null) return new ArrayList<IncidentDto>();
        return incidentEntities.stream().filter(inc -> inc != null).map(inc -> IncidentDto.createFrom(inc, (List)operations.get(inc.getKey()), processNames, (IncidentReader.IncidentDataHolder)incidentData.get(inc.getId()), null)).collect(Collectors.toList());
    }

    public static List<IncidentDto> sortDefault(List<IncidentDto> incidents) {
        Collections.sort(incidents, INCIDENT_DEFAULT_COMPARATOR);
        return incidents;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IncidentDto that = (IncidentDto)o;
        return this.hasActiveOperation == that.hasActiveOperation && Objects.equals(this.id, that.id) && Objects.equals(this.errorType, that.errorType) && Objects.equals(this.errorMessage, that.errorMessage) && Objects.equals(this.flowNodeId, that.flowNodeId) && Objects.equals(this.flowNodeInstanceId, that.flowNodeInstanceId) && Objects.equals(this.jobId, that.jobId) && Objects.equals(this.creationTime, that.creationTime) && Objects.equals(this.lastOperation, that.lastOperation) && Objects.equals(this.rootCauseInstance, that.rootCauseInstance) && Objects.equals(this.rootCauseDecision, that.rootCauseDecision);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.errorType, this.errorMessage, this.flowNodeId, this.flowNodeInstanceId, this.jobId, this.creationTime, this.hasActiveOperation, this.lastOperation, this.rootCauseInstance, this.rootCauseDecision);
    }
}
