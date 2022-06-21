package io.camunda.operate.webapp.rest;

import io.camunda.operate.entities.BatchOperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.rest.ValidLongId;
import io.camunda.operate.webapp.es.reader.ActivityStatisticsReader;
import io.camunda.operate.webapp.es.reader.FlowNodeInstanceReader;
import io.camunda.operate.webapp.es.reader.IncidentReader;
import io.camunda.operate.webapp.es.reader.ListViewReader;
import io.camunda.operate.webapp.es.reader.OperationReader;
import io.camunda.operate.webapp.es.reader.ProcessInstanceReader;
import io.camunda.operate.webapp.es.reader.SequenceFlowReader;
import io.camunda.operate.webapp.es.reader.VariableReader;
import io.camunda.operate.webapp.es.writer.BatchOperationWriter;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.ProcessInstanceCoreStatisticsDto;
import io.camunda.operate.webapp.rest.dto.SequenceFlowDto;
import io.camunda.operate.webapp.rest.dto.VariableRequestDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentResponseDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewRequestDto;
import io.camunda.operate.webapp.rest.dto.listview.ListViewResponseDto;
import io.camunda.operate.webapp.rest.dto.metadata.FlowNodeMetadataDto;
import io.camunda.operate.webapp.rest.dto.metadata.FlowNodeMetadataRequestDto;
import io.camunda.operate.webapp.rest.dto.operation.CreateBatchOperationRequestDto;
import io.camunda.operate.webapp.rest.dto.operation.CreateOperationRequestDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(
   tags = {"Process instances"}
)
@SwaggerDefinition(
   tags = {@Tag(
   name = "Process instances",
   description = "Process instances"
)}
)
@RestController
@RequestMapping({"/api/process-instances"})
@Validated
public class ProcessInstanceRestService {
   public static final String PROCESS_INSTANCE_URL = "/api/process-instances";
   @Autowired
   private BatchOperationWriter batchOperationWriter;
   @Autowired
   private ProcessInstanceReader processInstanceReader;
   @Autowired
   private ListViewReader listViewReader;
   @Autowired
   private ActivityStatisticsReader activityStatisticsReader;
   @Autowired
   private IncidentReader incidentReader;
   @Autowired
   private VariableReader variableReader;
   @Autowired
   private FlowNodeInstanceReader flowNodeInstanceReader;
   @Autowired
   private SequenceFlowReader sequenceFlowReader;
   @Autowired
   private OperationReader operationReader;

   @ApiOperation("Query process instances by different parameters")
   @PostMapping
   @Timed(
      value = "operate.query",
      extraTags = {"name", "processInstances"},
      description = "How long does it take to retrieve the processinstances by query."
   )
   public ListViewResponseDto queryProcessInstances(@RequestBody ListViewRequestDto processInstanceRequest) {
      if (processInstanceRequest.getQuery() == null) {
         throw new InvalidRequestException("Query must be provided.");
      } else if (processInstanceRequest.getQuery().getProcessVersion() != null && processInstanceRequest.getQuery().getBpmnProcessId() == null) {
         throw new InvalidRequestException("BpmnProcessId must be provided in request, when process version is not null.");
      } else {
         return this.listViewReader.queryProcessInstances(processInstanceRequest);
      }
   }

   @ApiOperation("Perform single operation on an instance (async)")
   @PostMapping({"/{id}/operation"})
   @PreAuthorize("hasPermission('write')")
   public BatchOperationEntity operation(@PathVariable @ValidLongId String id, @RequestBody CreateOperationRequestDto operationRequest) {
      this.validateOperationRequest(operationRequest, id);
      return this.batchOperationWriter.scheduleSingleOperation(Long.valueOf(id), operationRequest);
   }

   private void validateBatchOperationRequest(CreateBatchOperationRequestDto batchOperationRequest) {
      if (batchOperationRequest.getQuery() == null) {
         throw new InvalidRequestException("List view query must be defined.");
      } else if (batchOperationRequest.getOperationType() == null) {
         throw new InvalidRequestException("Operation type must be defined.");
      } else if (Set.of(OperationType.UPDATE_VARIABLE, OperationType.ADD_VARIABLE).contains(batchOperationRequest.getOperationType())) {
         throw new InvalidRequestException("For variable update use \"Create operation for one process instance\" endpoint.");
      }
   }

   private void validateOperationRequest(CreateOperationRequestDto operationRequest, @ValidLongId String processInstanceId) {
      if (operationRequest.getOperationType() == null) {
         throw new InvalidRequestException("Operation type must be defined.");
      } else if (!Set.of(OperationType.UPDATE_VARIABLE, OperationType.ADD_VARIABLE).contains(operationRequest.getOperationType()) || operationRequest.getVariableScopeId() != null && operationRequest.getVariableName() != null && !operationRequest.getVariableName().isEmpty() && operationRequest.getVariableValue() != null) {
         if (operationRequest.getOperationType().equals(OperationType.ADD_VARIABLE) && (this.variableReader.getVariableByName(processInstanceId, operationRequest.getVariableScopeId(), operationRequest.getVariableName()) != null || !this.operationReader.getOperations(OperationType.ADD_VARIABLE, processInstanceId, operationRequest.getVariableScopeId(), operationRequest.getVariableName()).isEmpty())) {
            throw new InvalidRequestException(String.format("Variable with the name \"%s\" already exists.", operationRequest.getVariableName()));
         }
      } else {
         throw new InvalidRequestException("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
      }
   }

   @ApiOperation("Create batch operation based on filter")
   @PostMapping({"/batch-operation"})
   @PreAuthorize("hasPermission('write')")
   public BatchOperationEntity createBatchOperation(@RequestBody CreateBatchOperationRequestDto batchOperationRequest) {
      this.validateBatchOperationRequest(batchOperationRequest);
      return this.batchOperationWriter.scheduleBatchOperation(batchOperationRequest);
   }

   @ApiOperation("Get process instance by id")
   @GetMapping({"/{id}"})
   public ListViewProcessInstanceDto queryProcessInstanceById(@PathVariable @ValidLongId String id) {
      return this.processInstanceReader.getProcessInstanceWithOperationsByKey(Long.valueOf(id));
   }

   @ApiOperation("Get incidents by process instance id")
   @GetMapping({"/{id}/incidents"})
   public IncidentResponseDto queryIncidentsByProcessInstanceId(@PathVariable @ValidLongId String id) {
      return this.incidentReader.getIncidentsByProcessInstanceId(id);
   }

   @ApiOperation("Get sequence flows by process instance id")
   @GetMapping({"/{id}/sequence-flows"})
   public List querySequenceFlowsByProcessInstanceId(@PathVariable @ValidLongId String id) {
      List sequenceFlows = this.sequenceFlowReader.getSequenceFlowsByProcessInstanceKey(Long.valueOf(id));
      return DtoCreator.create(sequenceFlows, SequenceFlowDto.class);
   }

   @ApiOperation("Get variables by process instance id and scope id")
   @PostMapping({"/{processInstanceId}/variables"})
   public List getVariables(@PathVariable @ValidLongId String processInstanceId, @RequestBody VariableRequestDto variableRequest) {
      this.validateRequest(variableRequest);
      return this.variableReader.getVariables(processInstanceId, variableRequest);
   }

   @ApiOperation("Get flow node states by process instance id")
   @GetMapping({"/{processInstanceId}/flow-node-states"})
   public Map getFlowNodeStates(@PathVariable @ValidLongId String processInstanceId) {
      return this.flowNodeInstanceReader.getFlowNodeStates(processInstanceId);
   }

   @ApiOperation("Get flow node metadata.")
   @PostMapping({"/{processInstanceId}/flow-node-metadata"})
   public FlowNodeMetadataDto getFlowNodeMetadata(@PathVariable @ValidLongId String processInstanceId, @RequestBody FlowNodeMetadataRequestDto request) {
      this.validateRequest(request);
      return this.flowNodeInstanceReader.getFlowNodeMetadata(processInstanceId, request);
   }

   private void validateRequest(VariableRequestDto request) {
      if (request.getScopeId() == null) {
         throw new InvalidRequestException("ScopeId must be specifies in the request.");
      }
   }

   private void validateRequest(FlowNodeMetadataRequestDto request) {
      if (request.getFlowNodeId() == null && request.getFlowNodeType() == null && request.getFlowNodeInstanceId() == null) {
         throw new InvalidRequestException("At least flowNodeId or flowNodeInstanceId must be specifies in the request.");
      } else if (request.getFlowNodeId() != null && request.getFlowNodeInstanceId() != null) {
         throw new InvalidRequestException("Only one of flowNodeId or flowNodeInstanceId must be specifies in the request.");
      }
   }

   @ApiOperation("Get activity instance statistics")
   @PostMapping(
      path = {"/statistics"}
   )
   public Collection getStatistics(@RequestBody ListViewQueryDto query) {
      List processDefinitionKeys = CollectionUtil.toSafeListOfLongs(query.getProcessIds());
      String bpmnProcessId = query.getBpmnProcessId();
      Integer processVersion = query.getProcessVersion();
      if ((processDefinitionKeys != null && processDefinitionKeys.size() == 1) == (bpmnProcessId != null && processVersion != null)) {
         throw new InvalidRequestException("Exactly one process must be specified in the request (via processIds or bpmnProcessId/version).");
      } else {
         return this.activityStatisticsReader.getFlowNodeStatistics(query);
      }
   }

   @ApiOperation("Get process instance core statistics (aggregations)")
   @GetMapping(
      path = {"/core-statistics"}
   )
   @Timed(
      value = "operate.query",
      extraTags = {"name", "corestatistics"},
      description = "How long does it take to retrieve the core statistics."
   )
   public ProcessInstanceCoreStatisticsDto getCoreStatistics() {
      return this.processInstanceReader.getCoreStatistics();
   }

   @ExceptionHandler({ConstraintViolationException.class})
   public ResponseEntity handleConstraintViolation(ConstraintViolationException exception) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
   }
}
