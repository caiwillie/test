package io.camunda.operate.entities.dmn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.operate.entities.OperateZeebeEntity;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DecisionInstanceEntity extends OperateZeebeEntity {
   private Integer executionIndex;
   private DecisionInstanceState state;
   private OffsetDateTime evaluationDate;
   private String evaluationFailure;
   private Long position;
   private long decisionRequirementsKey;
   private String decisionRequirementsId;
   private long processDefinitionKey;
   private long processInstanceKey;
   private long elementInstanceKey;
   private String elementId;
   private String decisionId;
   private String decisionDefinitionId;
   private String decisionName;
   private int decisionVersion;
   private String rootDecisionName;
   private String rootDecisionId;
   private String rootDecisionDefinitionId;
   private DecisionType decisionType;
   private String result;
   private List evaluatedInputs = new ArrayList();
   private List evaluatedOutputs = new ArrayList();
   @JsonIgnore
   private Object[] sortValues;

   public DecisionInstanceEntity setId(Long key, int executionIndex) {
      return (DecisionInstanceEntity)this.setId(String.format("%d-%d", key, executionIndex));
   }

   public static Long extractKey(String id) {
      return Long.valueOf(id.split("-")[0]);
   }

   public Integer getExecutionIndex() {
      return this.executionIndex;
   }

   public DecisionInstanceEntity setExecutionIndex(Integer executionIndex) {
      this.executionIndex = executionIndex;
      return this;
   }

   public DecisionInstanceState getState() {
      return this.state;
   }

   public DecisionInstanceEntity setState(DecisionInstanceState state) {
      this.state = state;
      return this;
   }

   public OffsetDateTime getEvaluationDate() {
      return this.evaluationDate;
   }

   public DecisionInstanceEntity setEvaluationDate(OffsetDateTime evaluationDate) {
      this.evaluationDate = evaluationDate;
      return this;
   }

   public String getEvaluationFailure() {
      return this.evaluationFailure;
   }

   public DecisionInstanceEntity setEvaluationFailure(String evaluationFailure) {
      this.evaluationFailure = evaluationFailure;
      return this;
   }

   public Long getPosition() {
      return this.position;
   }

   public DecisionInstanceEntity setPosition(Long position) {
      this.position = position;
      return this;
   }

   public String getDecisionDefinitionId() {
      return this.decisionDefinitionId;
   }

   public DecisionInstanceEntity setDecisionDefinitionId(String decisionDefinitionId) {
      this.decisionDefinitionId = decisionDefinitionId;
      return this;
   }

   public long getDecisionRequirementsKey() {
      return this.decisionRequirementsKey;
   }

   public DecisionInstanceEntity setDecisionRequirementsKey(long decisionRequirementsKey) {
      this.decisionRequirementsKey = decisionRequirementsKey;
      return this;
   }

   public String getDecisionRequirementsId() {
      return this.decisionRequirementsId;
   }

   public DecisionInstanceEntity setDecisionRequirementsId(String decisionRequirementsId) {
      this.decisionRequirementsId = decisionRequirementsId;
      return this;
   }

   public long getProcessDefinitionKey() {
      return this.processDefinitionKey;
   }

   public DecisionInstanceEntity setProcessDefinitionKey(long processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
      return this;
   }

   public long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public DecisionInstanceEntity setProcessInstanceKey(long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
      return this;
   }

   public long getElementInstanceKey() {
      return this.elementInstanceKey;
   }

   public DecisionInstanceEntity setElementInstanceKey(long elementInstanceKey) {
      this.elementInstanceKey = elementInstanceKey;
      return this;
   }

   public String getElementId() {
      return this.elementId;
   }

   public DecisionInstanceEntity setElementId(String elementId) {
      this.elementId = elementId;
      return this;
   }

   public String getDecisionId() {
      return this.decisionId;
   }

   public DecisionInstanceEntity setDecisionId(String decisionId) {
      this.decisionId = decisionId;
      return this;
   }

   public String getDecisionName() {
      return this.decisionName;
   }

   public DecisionInstanceEntity setDecisionName(String decisionName) {
      this.decisionName = decisionName;
      return this;
   }

   public int getDecisionVersion() {
      return this.decisionVersion;
   }

   public DecisionInstanceEntity setDecisionVersion(int decisionVersion) {
      this.decisionVersion = decisionVersion;
      return this;
   }

   public String getRootDecisionName() {
      return this.rootDecisionName;
   }

   public DecisionInstanceEntity setRootDecisionName(String rootDecisionName) {
      this.rootDecisionName = rootDecisionName;
      return this;
   }

   public String getRootDecisionId() {
      return this.rootDecisionId;
   }

   public DecisionInstanceEntity setRootDecisionId(String rootDecisionId) {
      this.rootDecisionId = rootDecisionId;
      return this;
   }

   public String getRootDecisionDefinitionId() {
      return this.rootDecisionDefinitionId;
   }

   public DecisionInstanceEntity setRootDecisionDefinitionId(String rootDecisionDefinitionId) {
      this.rootDecisionDefinitionId = rootDecisionDefinitionId;
      return this;
   }

   public DecisionType getDecisionType() {
      return this.decisionType;
   }

   public DecisionInstanceEntity setDecisionType(DecisionType decisionType) {
      this.decisionType = decisionType;
      return this;
   }

   public String getResult() {
      return this.result;
   }

   public DecisionInstanceEntity setResult(String result) {
      this.result = result;
      return this;
   }

   public List getEvaluatedInputs() {
      return this.evaluatedInputs;
   }

   public DecisionInstanceEntity setEvaluatedInputs(List evaluatedInputs) {
      this.evaluatedInputs = evaluatedInputs;
      return this;
   }

   public List getEvaluatedOutputs() {
      return this.evaluatedOutputs;
   }

   public DecisionInstanceEntity setEvaluatedOutputs(List evaluatedOutputs) {
      this.evaluatedOutputs = evaluatedOutputs;
      return this;
   }

   public Object[] getSortValues() {
      return this.sortValues;
   }

   public DecisionInstanceEntity setSortValues(Object[] sortValues) {
      this.sortValues = sortValues;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            DecisionInstanceEntity that = (DecisionInstanceEntity)o;
            return this.decisionRequirementsKey == that.decisionRequirementsKey && this.processDefinitionKey == that.processDefinitionKey && this.processInstanceKey == that.processInstanceKey && this.elementInstanceKey == that.elementInstanceKey && this.decisionVersion == that.decisionVersion && Objects.equals(this.executionIndex, that.executionIndex) && this.state == that.state && Objects.equals(this.evaluationDate, that.evaluationDate) && Objects.equals(this.evaluationFailure, that.evaluationFailure) && Objects.equals(this.position, that.position) && Objects.equals(this.decisionRequirementsId, that.decisionRequirementsId) && Objects.equals(this.elementId, that.elementId) && Objects.equals(this.decisionId, that.decisionId) && Objects.equals(this.decisionDefinitionId, that.decisionDefinitionId) && Objects.equals(this.decisionName, that.decisionName) && Objects.equals(this.rootDecisionName, that.rootDecisionName) && Objects.equals(this.rootDecisionId, that.rootDecisionId) && Objects.equals(this.rootDecisionDefinitionId, that.rootDecisionDefinitionId) && this.decisionType == that.decisionType && Objects.equals(this.result, that.result) && Objects.equals(this.evaluatedInputs, that.evaluatedInputs) && Objects.equals(this.evaluatedOutputs, that.evaluatedOutputs) && Arrays.equals(this.sortValues, that.sortValues);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result1 = Objects.hash(new Object[]{super.hashCode(), this.executionIndex, this.state, this.evaluationDate, this.evaluationFailure, this.position, this.decisionRequirementsKey, this.decisionRequirementsId, this.processDefinitionKey, this.processInstanceKey, this.elementInstanceKey, this.elementId, this.decisionId, this.decisionDefinitionId, this.decisionName, this.decisionVersion, this.rootDecisionName, this.rootDecisionId, this.rootDecisionDefinitionId, this.decisionType, this.result, this.evaluatedInputs, this.evaluatedOutputs});
      result1 = 31 * result1 + Arrays.hashCode(this.sortValues);
      return result1;
   }
}
