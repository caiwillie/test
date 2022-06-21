package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.entities.IncidentEntity;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.webapp.es.reader.IncidentReader;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.OperationDto;
import io.camunda.operate.webapp.rest.dto.ProcessInstanceReferenceDto;
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
   public static final Comparator INCIDENT_DEFAULT_COMPARATOR = (o1, o2) -> {
      return o1.getErrorType().equals(o2.getErrorType()) ? o1.getId().compareTo(o2.getId()) : o1.getErrorType().compareTo(o2.getErrorType());
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

   public static IncidentDto createFrom(IncidentEntity incidentEntity, Map processNames, IncidentReader.IncidentDataHolder incidentData, DecisionInstanceReferenceDto rootCauseDecision) {
      return createFrom(incidentEntity, Collections.emptyList(), processNames, incidentData, rootCauseDecision);
   }

   public static IncidentDto createFrom(IncidentEntity incidentEntity, List operations, Map processNames, IncidentReader.IncidentDataHolder incidentData, DecisionInstanceReferenceDto rootCauseDecision) {
      if (incidentEntity == null) {
         return null;
      } else {
         IncidentDto incident = (new IncidentDto()).setId(incidentEntity.getId()).setFlowNodeId(incidentEntity.getFlowNodeId()).setFlowNodeInstanceId(ConversionUtils.toStringOrNull(incidentEntity.getFlowNodeInstanceKey())).setErrorMessage(incidentEntity.getErrorMessage()).setErrorType(ErrorTypeDto.createFrom(incidentEntity.getErrorType())).setJobId(ConversionUtils.toStringOrNull(incidentEntity.getJobKey())).setCreationTime(incidentEntity.getCreationTime());
         if (operations != null && operations.size() > 0) {
            OperationEntity lastOperation = (OperationEntity)operations.get(0);
            incident.setLastOperation((OperationDto)DtoCreator.create((Object)lastOperation, OperationDto.class)).setHasActiveOperation(operations.stream().anyMatch((o) -> {
               return o.getState().equals(OperationState.SCHEDULED) || o.getState().equals(OperationState.LOCKED) || o.getState().equals(OperationState.SENT);
            }));
         }

         if (incidentData != null && incident.getFlowNodeInstanceId() != incidentData.getFinalFlowNodeInstanceId()) {
            incident.setFlowNodeId(incidentData.getFinalFlowNodeId());
            incident.setFlowNodeInstanceId(incidentData.getFinalFlowNodeInstanceId());
            ProcessInstanceReferenceDto rootCauseInstance = (new ProcessInstanceReferenceDto()).setInstanceId(String.valueOf(incidentEntity.getProcessInstanceKey())).setProcessDefinitionId(String.valueOf(incidentEntity.getProcessDefinitionKey()));
            if (processNames != null && processNames.get(incidentEntity.getProcessDefinitionKey()) != null) {
               rootCauseInstance.setProcessDefinitionName((String)processNames.get(incidentEntity.getProcessDefinitionKey()));
            } else {
               rootCauseInstance.setProcessDefinitionName("Unknown process");
            }

            incident.setRootCauseInstance(rootCauseInstance);
         }

         if (rootCauseDecision != null) {
            incident.setRootCauseDecision(rootCauseDecision);
         }

         return incident;
      }
   }

   public static List createFrom(List incidentEntities, Map operations, Map processNames, Map incidentData) {
      return (List)(incidentEntities != null ? (List)incidentEntities.stream().filter((inc) -> {
         return inc != null;
      }).map((inc) -> {
         return createFrom(inc, (List)operations.get(inc.getKey()), processNames, (IncidentReader.IncidentDataHolder)incidentData.get(inc.getId()), (DecisionInstanceReferenceDto)null);
      }).collect(Collectors.toList()) : new ArrayList());
   }

   public static List sortDefault(List incidents) {
      Collections.sort(incidents, INCIDENT_DEFAULT_COMPARATOR);
      return incidents;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         IncidentDto that = (IncidentDto)o;
         return this.hasActiveOperation == that.hasActiveOperation && Objects.equals(this.id, that.id) && Objects.equals(this.errorType, that.errorType) && Objects.equals(this.errorMessage, that.errorMessage) && Objects.equals(this.flowNodeId, that.flowNodeId) && Objects.equals(this.flowNodeInstanceId, that.flowNodeInstanceId) && Objects.equals(this.jobId, that.jobId) && Objects.equals(this.creationTime, that.creationTime) && Objects.equals(this.lastOperation, that.lastOperation) && Objects.equals(this.rootCauseInstance, that.rootCauseInstance) && Objects.equals(this.rootCauseDecision, that.rootCauseDecision);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.errorType, this.errorMessage, this.flowNodeId, this.flowNodeInstanceId, this.jobId, this.creationTime, this.hasActiveOperation, this.lastOperation, this.rootCauseInstance, this.rootCauseDecision});
   }
}
