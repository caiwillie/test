package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.DecisionInstanceEntity;
import io.camunda.operate.entities.dmn.DecisionType;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class DecisionInstanceDto implements CreatableFromEntity {
   public static final Comparator DECISION_INSTANCE_OUTPUT_DTO_COMPARATOR = Comparator.comparingInt(DecisionInstanceOutputDto::getRuleIndex).thenComparing(DecisionInstanceOutputDto::getName);
   public static final Comparator DECISION_INSTANCE_INPUT_DTO_COMPARATOR = Comparator.comparing(DecisionInstanceInputDto::getName);
   private String id;
   private DecisionInstanceStateDto state;
   private DecisionType decisionType;
   private String decisionDefinitionId;
   private String decisionId;
   private String decisionName;
   private int decisionVersion;
   private OffsetDateTime evaluationDate;
   private String errorMessage;
   private String processInstanceId;
   private String result;
   private List evaluatedInputs;
   private List evaluatedOutputs;

   public String getId() {
      return this.id;
   }

   public DecisionInstanceDto setId(String id) {
      this.id = id;
      return this;
   }

   public DecisionInstanceStateDto getState() {
      return this.state;
   }

   public DecisionInstanceDto setState(DecisionInstanceStateDto state) {
      this.state = state;
      return this;
   }

   public DecisionType getDecisionType() {
      return this.decisionType;
   }

   public DecisionInstanceDto setDecisionType(DecisionType decisionType) {
      this.decisionType = decisionType;
      return this;
   }

   public String getDecisionDefinitionId() {
      return this.decisionDefinitionId;
   }

   public DecisionInstanceDto setDecisionDefinitionId(String decisionDefinitionId) {
      this.decisionDefinitionId = decisionDefinitionId;
      return this;
   }

   public String getDecisionId() {
      return this.decisionId;
   }

   public DecisionInstanceDto setDecisionId(String decisionId) {
      this.decisionId = decisionId;
      return this;
   }

   public String getDecisionName() {
      return this.decisionName;
   }

   public DecisionInstanceDto setDecisionName(String decisionName) {
      this.decisionName = decisionName;
      return this;
   }

   public int getDecisionVersion() {
      return this.decisionVersion;
   }

   public DecisionInstanceDto setDecisionVersion(int decisionVersion) {
      this.decisionVersion = decisionVersion;
      return this;
   }

   public OffsetDateTime getEvaluationDate() {
      return this.evaluationDate;
   }

   public DecisionInstanceDto setEvaluationDate(OffsetDateTime evaluationDate) {
      this.evaluationDate = evaluationDate;
      return this;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public DecisionInstanceDto setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
   }

   public String getProcessInstanceId() {
      return this.processInstanceId;
   }

   public DecisionInstanceDto setProcessInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
      return this;
   }

   public String getResult() {
      return this.result;
   }

   public DecisionInstanceDto setResult(String result) {
      this.result = result;
      return this;
   }

   public List getEvaluatedInputs() {
      return this.evaluatedInputs;
   }

   public DecisionInstanceDto setEvaluatedInputs(List evaluatedInputs) {
      this.evaluatedInputs = evaluatedInputs;
      return this;
   }

   public List getEvaluatedOutputs() {
      return this.evaluatedOutputs;
   }

   public DecisionInstanceDto setEvaluatedOutputs(List evaluatedOutputs) {
      this.evaluatedOutputs = evaluatedOutputs;
      return this;
   }

   public DecisionInstanceDto fillFrom(DecisionInstanceEntity entity) {
      List inputs = DtoCreator.create(entity.getEvaluatedInputs(), DecisionInstanceInputDto.class);
      Collections.sort(inputs, DECISION_INSTANCE_INPUT_DTO_COMPARATOR);
      List outputs = DtoCreator.create(entity.getEvaluatedOutputs(), DecisionInstanceOutputDto.class);
      Collections.sort(outputs, DECISION_INSTANCE_OUTPUT_DTO_COMPARATOR);
      this.setId(entity.getId()).setDecisionDefinitionId(entity.getDecisionDefinitionId()).setDecisionId(entity.getDecisionId()).setDecisionName(entity.getDecisionName()).setDecisionType(entity.getDecisionType()).setDecisionVersion(entity.getDecisionVersion()).setErrorMessage(entity.getEvaluationFailure()).setEvaluationDate(entity.getEvaluationDate()).setEvaluatedInputs(inputs).setEvaluatedOutputs(outputs).setProcessInstanceId(String.valueOf(entity.getProcessInstanceKey())).setResult(entity.getResult()).setState(DecisionInstanceStateDto.getState(entity.getState()));
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionInstanceDto that = (DecisionInstanceDto)o;
         return this.decisionVersion == that.decisionVersion && this.processInstanceId == that.processInstanceId && Objects.equals(this.id, that.id) && this.state == that.state && this.decisionType == that.decisionType && Objects.equals(this.decisionDefinitionId, that.decisionDefinitionId) && Objects.equals(this.decisionId, that.decisionId) && Objects.equals(this.decisionName, that.decisionName) && Objects.equals(this.evaluationDate, that.evaluationDate) && Objects.equals(this.errorMessage, that.errorMessage) && Objects.equals(this.result, that.result) && Objects.equals(this.evaluatedInputs, that.evaluatedInputs) && Objects.equals(this.evaluatedOutputs, that.evaluatedOutputs);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.state, this.decisionType, this.decisionDefinitionId, this.decisionId, this.decisionName, this.decisionVersion, this.evaluationDate, this.errorMessage, this.processInstanceId, this.result, this.evaluatedInputs, this.evaluatedOutputs});
   }
}
