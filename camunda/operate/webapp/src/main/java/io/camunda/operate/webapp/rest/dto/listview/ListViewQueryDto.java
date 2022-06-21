package io.camunda.operate.webapp.rest.dto.listview;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@ApiModel("Process instance query")
public class ListViewQueryDto {
   private boolean running;
   private boolean active;
   private boolean incidents;
   private boolean finished;
   private boolean completed;
   private boolean canceled;
   @ApiModelProperty(
      value = "Array of process instance ids",
      allowEmptyValue = true
   )
   private List ids;
   private String errorMessage;
   private String activityId;
   @ApiModelProperty(
      value = "Start date after (inclusive)",
      allowEmptyValue = true
   )
   private OffsetDateTime startDateAfter;
   @ApiModelProperty(
      value = "Start date before (exclusive)",
      allowEmptyValue = true
   )
   private OffsetDateTime startDateBefore;
   @ApiModelProperty(
      value = "End date after (inclusive)",
      allowEmptyValue = true
   )
   private OffsetDateTime endDateAfter;
   @ApiModelProperty(
      value = "End date before (exclusive)",
      allowEmptyValue = true
   )
   private OffsetDateTime endDateBefore;
   private List processIds;
   private String bpmnProcessId;
   @ApiModelProperty(
      value = "Process version, goes together with bpmnProcessId. Can be null, then all version of the process are selected.",
      allowEmptyValue = true
   )
   private Integer processVersion;
   private List excludeIds;
   private VariablesQueryDto variable;
   private String batchOperationId;
   private Long parentInstanceId;

   public boolean isRunning() {
      return this.running;
   }

   public ListViewQueryDto setRunning(boolean running) {
      this.running = running;
      return this;
   }

   public boolean isCompleted() {
      return this.completed;
   }

   public ListViewQueryDto setCompleted(boolean completed) {
      this.completed = completed;
      return this;
   }

   public boolean isIncidents() {
      return this.incidents;
   }

   public ListViewQueryDto setIncidents(boolean incidents) {
      this.incidents = incidents;
      return this;
   }

   public boolean isActive() {
      return this.active;
   }

   public ListViewQueryDto setActive(boolean active) {
      this.active = active;
      return this;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public ListViewQueryDto setFinished(boolean finished) {
      this.finished = finished;
      return this;
   }

   public boolean isCanceled() {
      return this.canceled;
   }

   public ListViewQueryDto setCanceled(boolean canceled) {
      this.canceled = canceled;
      return this;
   }

   public List getIds() {
      return this.ids;
   }

   public ListViewQueryDto setIds(List ids) {
      this.ids = ids;
      return this;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public ListViewQueryDto setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
   }

   public String getActivityId() {
      return this.activityId;
   }

   public ListViewQueryDto setActivityId(String activityId) {
      this.activityId = activityId;
      return this;
   }

   public OffsetDateTime getStartDateAfter() {
      return this.startDateAfter;
   }

   public ListViewQueryDto setStartDateAfter(OffsetDateTime startDateAfter) {
      this.startDateAfter = startDateAfter;
      return this;
   }

   public OffsetDateTime getStartDateBefore() {
      return this.startDateBefore;
   }

   public ListViewQueryDto setStartDateBefore(OffsetDateTime startDateBefore) {
      this.startDateBefore = startDateBefore;
      return this;
   }

   public OffsetDateTime getEndDateAfter() {
      return this.endDateAfter;
   }

   public ListViewQueryDto setEndDateAfter(OffsetDateTime endDateAfter) {
      this.endDateAfter = endDateAfter;
      return this;
   }

   public OffsetDateTime getEndDateBefore() {
      return this.endDateBefore;
   }

   public ListViewQueryDto setEndDateBefore(OffsetDateTime endDateBefore) {
      this.endDateBefore = endDateBefore;
      return this;
   }

   public List getProcessIds() {
      return this.processIds;
   }

   public ListViewQueryDto setProcessIds(List processIds) {
      this.processIds = processIds;
      return this;
   }

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public ListViewQueryDto setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
      return this;
   }

   public Integer getProcessVersion() {
      return this.processVersion;
   }

   public ListViewQueryDto setProcessVersion(Integer processVersion) {
      this.processVersion = processVersion;
      return this;
   }

   public List getExcludeIds() {
      return this.excludeIds;
   }

   public ListViewQueryDto setExcludeIds(List excludeIds) {
      this.excludeIds = excludeIds;
      return this;
   }

   public VariablesQueryDto getVariable() {
      return this.variable;
   }

   public ListViewQueryDto setVariable(VariablesQueryDto variable) {
      this.variable = variable;
      return this;
   }

   public String getBatchOperationId() {
      return this.batchOperationId;
   }

   public void setBatchOperationId(String batchOperationId) {
      this.batchOperationId = batchOperationId;
   }

   public Long getParentInstanceId() {
      return this.parentInstanceId;
   }

   public ListViewQueryDto setParentInstanceId(Long parentInstanceId) {
      this.parentInstanceId = parentInstanceId;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ListViewQueryDto that = (ListViewQueryDto)o;
         return this.running == that.running && this.active == that.active && this.incidents == that.incidents && this.finished == that.finished && this.completed == that.completed && this.canceled == that.canceled && Objects.equals(this.ids, that.ids) && Objects.equals(this.errorMessage, that.errorMessage) && Objects.equals(this.activityId, that.activityId) && Objects.equals(this.startDateAfter, that.startDateAfter) && Objects.equals(this.startDateBefore, that.startDateBefore) && Objects.equals(this.endDateAfter, that.endDateAfter) && Objects.equals(this.endDateBefore, that.endDateBefore) && Objects.equals(this.processIds, that.processIds) && Objects.equals(this.bpmnProcessId, that.bpmnProcessId) && Objects.equals(this.processVersion, that.processVersion) && Objects.equals(this.excludeIds, that.excludeIds) && Objects.equals(this.variable, that.variable) && Objects.equals(this.batchOperationId, that.batchOperationId) && Objects.equals(this.parentInstanceId, that.parentInstanceId);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.running, this.active, this.incidents, this.finished, this.completed, this.canceled, this.ids, this.errorMessage, this.activityId, this.startDateAfter, this.startDateBefore, this.endDateAfter, this.endDateBefore, this.processIds, this.bpmnProcessId, this.processVersion, this.excludeIds, this.variable, this.batchOperationId, this.parentInstanceId});
   }
}
