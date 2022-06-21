package io.camunda.operate.entities.listview;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.operate.entities.OperateZeebeEntity;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProcessInstanceForListViewEntity extends OperateZeebeEntity {
   private Long processDefinitionKey;
   private String processName;
   private Integer processVersion;
   private String bpmnProcessId;
   private OffsetDateTime startDate;
   private OffsetDateTime endDate;
   private ProcessInstanceState state;
   private List batchOperationIds;
   private Long parentProcessInstanceKey;
   private Long parentFlowNodeInstanceKey;
   private String treePath;
   private boolean incident;
   private ListViewJoinRelation joinRelation = new ListViewJoinRelation("processInstance");
   @JsonIgnore
   private Object[] sortValues;

   public Long getProcessInstanceKey() {
      return this.getKey();
   }

   public void setProcessInstanceKey(Long processInstanceKey) {
      this.setKey(processInstanceKey);
   }

   public Long getProcessDefinitionKey() {
      return this.processDefinitionKey;
   }

   public void setProcessDefinitionKey(Long processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
   }

   public String getProcessName() {
      return this.processName;
   }

   public void setProcessName(String processName) {
      this.processName = processName;
   }

   public Integer getProcessVersion() {
      return this.processVersion;
   }

   public void setProcessVersion(Integer processVersion) {
      this.processVersion = processVersion;
   }

   public OffsetDateTime getStartDate() {
      return this.startDate;
   }

   public void setStartDate(OffsetDateTime startDate) {
      this.startDate = startDate;
   }

   public OffsetDateTime getEndDate() {
      return this.endDate;
   }

   public ProcessInstanceForListViewEntity setEndDate(OffsetDateTime endDate) {
      this.endDate = endDate;
      return this;
   }

   public ProcessInstanceState getState() {
      return this.state;
   }

   public ProcessInstanceForListViewEntity setState(ProcessInstanceState state) {
      this.state = state;
      return this;
   }

   public List getBatchOperationIds() {
      return this.batchOperationIds;
   }

   public void setBatchOperationIds(List batchOperationIds) {
      this.batchOperationIds = batchOperationIds;
   }

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public void setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
   }

   public Long getParentProcessInstanceKey() {
      return this.parentProcessInstanceKey;
   }

   public ProcessInstanceForListViewEntity setParentProcessInstanceKey(Long parentProcessInstanceKey) {
      this.parentProcessInstanceKey = parentProcessInstanceKey;
      return this;
   }

   public Long getParentFlowNodeInstanceKey() {
      return this.parentFlowNodeInstanceKey;
   }

   public ProcessInstanceForListViewEntity setParentFlowNodeInstanceKey(Long parentFlowNodeInstanceKey) {
      this.parentFlowNodeInstanceKey = parentFlowNodeInstanceKey;
      return this;
   }

   public String getTreePath() {
      return this.treePath;
   }

   public ProcessInstanceForListViewEntity setTreePath(String treePath) {
      this.treePath = treePath;
      return this;
   }

   public boolean isIncident() {
      return this.incident;
   }

   public ProcessInstanceForListViewEntity setIncident(boolean incident) {
      this.incident = incident;
      return this;
   }

   public ListViewJoinRelation getJoinRelation() {
      return this.joinRelation;
   }

   public void setJoinRelation(ListViewJoinRelation joinRelation) {
      this.joinRelation = joinRelation;
   }

   public Object[] getSortValues() {
      return this.sortValues;
   }

   public void setSortValues(Object[] sortValues) {
      this.sortValues = sortValues;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            ProcessInstanceForListViewEntity that = (ProcessInstanceForListViewEntity)o;
            return this.incident == that.incident && Objects.equals(this.processDefinitionKey, that.processDefinitionKey) && Objects.equals(this.processName, that.processName) && Objects.equals(this.processVersion, that.processVersion) && Objects.equals(this.bpmnProcessId, that.bpmnProcessId) && Objects.equals(this.startDate, that.startDate) && Objects.equals(this.endDate, that.endDate) && this.state == that.state && Objects.equals(this.batchOperationIds, that.batchOperationIds) && Objects.equals(this.parentProcessInstanceKey, that.parentProcessInstanceKey) && Objects.equals(this.parentFlowNodeInstanceKey, that.parentFlowNodeInstanceKey) && Objects.equals(this.treePath, that.treePath) && Objects.equals(this.joinRelation, that.joinRelation) && Arrays.equals(this.sortValues, that.sortValues);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{super.hashCode(), this.processDefinitionKey, this.processName, this.processVersion, this.bpmnProcessId, this.startDate, this.endDate, this.state, this.batchOperationIds, this.parentProcessInstanceKey, this.parentFlowNodeInstanceKey, this.treePath, this.incident, this.joinRelation});
      result = 31 * result + Arrays.hashCode(this.sortValues);
      return result;
   }
}
