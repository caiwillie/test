package io.camunda.operate.webapp.rest.dto.dmn.list;

import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class DecisionInstanceListQueryDto {
   private List decisionDefinitionIds;
   private boolean evaluated;
   private boolean failed;
   private List ids;
   private String processInstanceId;
   @ApiModelProperty(
      value = "Evaluation date after (inclusive)",
      allowEmptyValue = true
   )
   private OffsetDateTime evaluationDateAfter;
   @ApiModelProperty(
      value = "Evaluation date after (inclusive)",
      allowEmptyValue = true
   )
   private OffsetDateTime evaluationDateBefore;

   public List getDecisionDefinitionIds() {
      return this.decisionDefinitionIds;
   }

   public DecisionInstanceListQueryDto setDecisionDefinitionIds(List decisionDefinitionIds) {
      this.decisionDefinitionIds = decisionDefinitionIds;
      return this;
   }

   public boolean isEvaluated() {
      return this.evaluated;
   }

   public DecisionInstanceListQueryDto setEvaluated(boolean evaluated) {
      this.evaluated = evaluated;
      return this;
   }

   public boolean isFailed() {
      return this.failed;
   }

   public DecisionInstanceListQueryDto setFailed(boolean failed) {
      this.failed = failed;
      return this;
   }

   public List getIds() {
      return this.ids;
   }

   public DecisionInstanceListQueryDto setIds(List ids) {
      this.ids = ids;
      return this;
   }

   public String getProcessInstanceId() {
      return this.processInstanceId;
   }

   public DecisionInstanceListQueryDto setProcessInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
      return this;
   }

   public OffsetDateTime getEvaluationDateAfter() {
      return this.evaluationDateAfter;
   }

   public DecisionInstanceListQueryDto setEvaluationDateAfter(OffsetDateTime evaluationDateAfter) {
      this.evaluationDateAfter = evaluationDateAfter;
      return this;
   }

   public OffsetDateTime getEvaluationDateBefore() {
      return this.evaluationDateBefore;
   }

   public DecisionInstanceListQueryDto setEvaluationDateBefore(OffsetDateTime evaluationDateBefore) {
      this.evaluationDateBefore = evaluationDateBefore;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionInstanceListQueryDto that = (DecisionInstanceListQueryDto)o;
         return this.evaluated == that.evaluated && this.failed == that.failed && Objects.equals(this.decisionDefinitionIds, that.decisionDefinitionIds) && Objects.equals(this.ids, that.ids) && Objects.equals(this.processInstanceId, that.processInstanceId) && Objects.equals(this.evaluationDateAfter, that.evaluationDateAfter) && Objects.equals(this.evaluationDateBefore, that.evaluationDateBefore);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.decisionDefinitionIds, this.evaluated, this.failed, this.ids, this.processInstanceId, this.evaluationDateAfter, this.evaluationDateBefore});
   }

   public String toString() {
      return "DecisionInstanceListQueryDto{decisionDefinitionIds=" + this.decisionDefinitionIds + ", evaluated=" + this.evaluated + ", failed=" + this.failed + ", ids=" + this.ids + ", processInstanceId='" + this.processInstanceId + "', evaluationDateAfter=" + this.evaluationDateAfter + ", evaluationDateBefore=" + this.evaluationDateBefore + "}";
   }
}
