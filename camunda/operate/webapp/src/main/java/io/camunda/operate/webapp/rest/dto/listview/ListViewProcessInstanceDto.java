package io.camunda.operate.webapp.rest.dto.listview;

import io.camunda.operate.entities.OperationState;
import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.OperationDto;
import io.camunda.operate.zeebeimport.util.TreePath;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListViewProcessInstanceDto {
   private String id;
   private String processId;
   private String processName;
   private Integer processVersion;
   private OffsetDateTime startDate;
   private OffsetDateTime endDate;
   private ProcessInstanceStateDto state;
   private String bpmnProcessId;
   private boolean hasActiveOperation = false;
   private List operations = new ArrayList();
   private String parentInstanceId;
   private String rootInstanceId;
   private List callHierarchy = new ArrayList();
   private String[] sortValues;

   public String getId() {
      return this.id;
   }

   public ListViewProcessInstanceDto setId(String id) {
      this.id = id;
      return this;
   }

   public String getProcessId() {
      return this.processId;
   }

   public ListViewProcessInstanceDto setProcessId(String processId) {
      this.processId = processId;
      return this;
   }

   public String getProcessName() {
      return this.processName;
   }

   public ListViewProcessInstanceDto setProcessName(String processName) {
      this.processName = processName;
      return this;
   }

   public Integer getProcessVersion() {
      return this.processVersion;
   }

   public ListViewProcessInstanceDto setProcessVersion(Integer processVersion) {
      this.processVersion = processVersion;
      return this;
   }

   public OffsetDateTime getStartDate() {
      return this.startDate;
   }

   public ListViewProcessInstanceDto setStartDate(OffsetDateTime startDate) {
      this.startDate = startDate;
      return this;
   }

   public OffsetDateTime getEndDate() {
      return this.endDate;
   }

   public ListViewProcessInstanceDto setEndDate(OffsetDateTime endDate) {
      this.endDate = endDate;
      return this;
   }

   public ProcessInstanceStateDto getState() {
      return this.state;
   }

   public ListViewProcessInstanceDto setState(ProcessInstanceStateDto state) {
      this.state = state;
      return this;
   }

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public ListViewProcessInstanceDto setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
      return this;
   }

   public boolean isHasActiveOperation() {
      return this.hasActiveOperation;
   }

   public ListViewProcessInstanceDto setHasActiveOperation(boolean hasActiveOperation) {
      this.hasActiveOperation = hasActiveOperation;
      return this;
   }

   public List getOperations() {
      return this.operations;
   }

   public ListViewProcessInstanceDto setOperations(List operations) {
      this.operations = operations;
      return this;
   }

   public String getParentInstanceId() {
      return this.parentInstanceId;
   }

   public ListViewProcessInstanceDto setParentInstanceId(String parentInstanceId) {
      this.parentInstanceId = parentInstanceId;
      return this;
   }

   public List getCallHierarchy() {
      return this.callHierarchy;
   }

   public ListViewProcessInstanceDto setCallHierarchy(List callHierarchy) {
      this.callHierarchy = callHierarchy;
      return this;
   }

   public String getRootInstanceId() {
      return this.rootInstanceId;
   }

   public ListViewProcessInstanceDto setRootInstanceId(String rootInstanceId) {
      this.rootInstanceId = rootInstanceId;
      return this;
   }

   public String[] getSortValues() {
      return this.sortValues;
   }

   public ListViewProcessInstanceDto setSortValues(String[] sortValues) {
      this.sortValues = sortValues;
      return this;
   }

   public static ListViewProcessInstanceDto createFrom(ProcessInstanceForListViewEntity processInstanceEntity, List operations) {
      return createFrom(processInstanceEntity, operations, (List)null);
   }

   public static ListViewProcessInstanceDto createFrom(ProcessInstanceForListViewEntity processInstanceEntity, List operations, List callHierarchy) {
      if (processInstanceEntity == null) {
         return null;
      } else {
         ListViewProcessInstanceDto processInstance = new ListViewProcessInstanceDto();
         processInstance.setId(processInstanceEntity.getId()).setStartDate(processInstanceEntity.getStartDate()).setEndDate(processInstanceEntity.getEndDate());
         if (processInstanceEntity.getState() == ProcessInstanceState.ACTIVE && processInstanceEntity.isIncident()) {
            processInstance.setState(ProcessInstanceStateDto.INCIDENT);
         } else {
            processInstance.setState(ProcessInstanceStateDto.getState(processInstanceEntity.getState()));
         }

         processInstance.setProcessId(ConversionUtils.toStringOrNull(processInstanceEntity.getProcessDefinitionKey())).setBpmnProcessId(processInstanceEntity.getBpmnProcessId()).setProcessName(processInstanceEntity.getProcessName()).setProcessVersion(processInstanceEntity.getProcessVersion()).setOperations(DtoCreator.create(operations, OperationDto.class));
         if (operations != null) {
            processInstance.setHasActiveOperation(operations.stream().anyMatch((o) -> {
               return o.getState().equals(OperationState.SCHEDULED) || o.getState().equals(OperationState.LOCKED) || o.getState().equals(OperationState.SENT);
            }));
         }

         if (processInstanceEntity.getParentProcessInstanceKey() != null) {
            processInstance.setParentInstanceId(String.valueOf(processInstanceEntity.getParentProcessInstanceKey()));
         }

         if (processInstanceEntity.getSortValues() != null) {
            processInstance.setSortValues((String[])Arrays.stream(processInstanceEntity.getSortValues()).map(String::valueOf).toArray((x$0) -> {
               return new String[x$0];
            }));
         }

         if (processInstanceEntity.getTreePath() != null) {
            String rootInstanceId = (new TreePath(processInstanceEntity.getTreePath())).extractRootInstanceId();
            if (!processInstanceEntity.getId().equals(rootInstanceId)) {
               processInstance.setRootInstanceId(rootInstanceId);
            }
         }

         processInstance.setCallHierarchy(callHierarchy);
         return processInstance;
      }
   }

   public static List createFrom(List processInstanceEntities, Map operationsPerProcessInstance) {
      return (List)(processInstanceEntities == null ? new ArrayList() : (List)processInstanceEntities.stream().filter((item) -> {
         return item != null;
      }).map((item) -> {
         return createFrom(item, (List)operationsPerProcessInstance.get(item.getProcessInstanceKey()));
      }).collect(Collectors.toList()));
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ListViewProcessInstanceDto that = (ListViewProcessInstanceDto)o;
         return this.hasActiveOperation == that.hasActiveOperation && Objects.equals(this.id, that.id) && Objects.equals(this.processId, that.processId) && Objects.equals(this.processName, that.processName) && Objects.equals(this.processVersion, that.processVersion) && Objects.equals(this.startDate, that.startDate) && Objects.equals(this.endDate, that.endDate) && this.state == that.state && Objects.equals(this.bpmnProcessId, that.bpmnProcessId) && Objects.equals(this.operations, that.operations) && Objects.equals(this.parentInstanceId, that.parentInstanceId) && Objects.equals(this.rootInstanceId, that.rootInstanceId) && Objects.equals(this.callHierarchy, that.callHierarchy) && Arrays.equals(this.sortValues, that.sortValues);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{this.id, this.processId, this.processName, this.processVersion, this.startDate, this.endDate, this.state, this.bpmnProcessId, this.hasActiveOperation, this.operations, this.parentInstanceId, this.rootInstanceId, this.callHierarchy});
      result = 31 * result + Arrays.hashCode(this.sortValues);
      return result;
   }

   public String toString() {
      return String.format("ListViewProcessInstanceDto %s (%s)", this.processName, this.processId);
   }
}
