/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.BatchOperationEntity
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.util.rest.ValidLongId
 *  io.camunda.operate.webapp.es.reader.ActivityStatisticsReader
 *  io.camunda.operate.webapp.es.reader.FlowNodeInstanceReader
 *  io.camunda.operate.webapp.es.reader.IncidentReader
 *  io.camunda.operate.webapp.es.reader.ListViewReader
 *  io.camunda.operate.webapp.es.reader.OperationReader
 *  io.camunda.operate.webapp.es.reader.ProcessInstanceReader
 *  io.camunda.operate.webapp.es.reader.SequenceFlowReader
 *  io.camunda.operate.webapp.es.reader.VariableReader
 *  io.camunda.operate.webapp.es.writer.BatchOperationWriter
 *  io.camunda.operate.webapp.rest.dto.DtoCreator
 *  io.camunda.operate.webapp.rest.dto.FlowNodeStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.ProcessInstanceCoreStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.SequenceFlowDto
 *  io.camunda.operate.webapp.rest.dto.VariableDto
 *  io.camunda.operate.webapp.rest.dto.VariableRequestDto
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeStateDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentResponseDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewProcessInstanceDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewRequestDto
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewResponseDto
 *  io.camunda.operate.webapp.rest.dto.metadata.FlowNodeMetadataDto
 *  io.camunda.operate.webapp.rest.dto.metadata.FlowNodeMetadataRequestDto
 *  io.camunda.operate.webapp.rest.dto.operation.CreateBatchOperationRequestDto
 *  io.camunda.operate.webapp.rest.dto.operation.CreateOperationRequestDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  io.micrometer.core.annotation.Timed
 *  io.swagger.annotations.Api
 *  io.swagger.annotations.ApiOperation
 *  io.swagger.annotations.SwaggerDefinition
 *  io.swagger.annotations.Tag
 *  javax.validation.ConstraintViolationException
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
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
import io.camunda.operate.webapp.rest.dto.FlowNodeStatisticsDto;
import io.camunda.operate.webapp.rest.dto.ProcessInstanceCoreStatisticsDto;
import io.camunda.operate.webapp.rest.dto.SequenceFlowDto;
import io.camunda.operate.webapp.rest.dto.VariableDto;
import io.camunda.operate.webapp.rest.dto.VariableRequestDto;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeStateDto;
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

@Api(tags={"Process instances"})
@SwaggerDefinition(tags={@Tag(name="Process instances", description="Process instances")})
@RestController
@RequestMapping(value={"/api/process-instances"})
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

    @ApiOperation(value="Query process instances by different parameters")
    @PostMapping
    @Timed(value="operate.query", extraTags={"name", "processInstances"}, description="How long does it take to retrieve the processinstances by query.")
    public ListViewResponseDto queryProcessInstances(@RequestBody ListViewRequestDto processInstanceRequest) {
        if (processInstanceRequest.getQuery() == null) {
            throw new InvalidRequestException("Query must be provided.");
        }
        if (processInstanceRequest.getQuery().getProcessVersion() == null) return this.listViewReader.queryProcessInstances(processInstanceRequest);
        if (processInstanceRequest.getQuery().getBpmnProcessId() != null) return this.listViewReader.queryProcessInstances(processInstanceRequest);
        throw new InvalidRequestException("BpmnProcessId must be provided in request, when process version is not null.");
    }

    @ApiOperation(value="Perform single operation on an instance (async)")
    @PostMapping(value={"/{id}/operation"})
    @PreAuthorize(value="hasPermission('write')")
    public BatchOperationEntity operation(@PathVariable @ValidLongId String id, @RequestBody CreateOperationRequestDto operationRequest) {
        this.validateOperationRequest(operationRequest, id);
        return this.batchOperationWriter.scheduleSingleOperation(Long.valueOf(id).longValue(), operationRequest);
    }

    private void validateBatchOperationRequest(CreateBatchOperationRequestDto batchOperationRequest) {
        if (batchOperationRequest.getQuery() == null) {
            throw new InvalidRequestException("List view query must be defined.");
        }
        if (batchOperationRequest.getOperationType() == null) {
            throw new InvalidRequestException("Operation type must be defined.");
        }
        if (!Set.of(OperationType.UPDATE_VARIABLE, OperationType.ADD_VARIABLE).contains(batchOperationRequest.getOperationType())) return;
        throw new InvalidRequestException("For variable update use \"Create operation for one process instance\" endpoint.");
    }

    private void validateOperationRequest(CreateOperationRequestDto operationRequest, @ValidLongId String processInstanceId) {
        if (operationRequest.getOperationType() == null) {
            throw new InvalidRequestException("Operation type must be defined.");
        }
        if (Set.of(OperationType.UPDATE_VARIABLE, OperationType.ADD_VARIABLE).contains(operationRequest.getOperationType())) {
            if (operationRequest.getVariableScopeId() == null) throw new InvalidRequestException("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
            if (operationRequest.getVariableName() == null) throw new InvalidRequestException("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
            if (operationRequest.getVariableName().isEmpty()) throw new InvalidRequestException("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
            if (operationRequest.getVariableValue() == null) {
                throw new InvalidRequestException("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
            }
        }
        if (!operationRequest.getOperationType().equals((Object)OperationType.ADD_VARIABLE)) return;
        if (this.variableReader.getVariableByName(processInstanceId, operationRequest.getVariableScopeId(), operationRequest.getVariableName()) != null) throw new InvalidRequestException(String.format("Variable with the name \"%s\" already exists.", operationRequest.getVariableName()));
        if (this.operationReader.getOperations(OperationType.ADD_VARIABLE, processInstanceId, operationRequest.getVariableScopeId(), operationRequest.getVariableName()).isEmpty()) return;
        throw new InvalidRequestException(String.format("Variable with the name \"%s\" already exists.", operationRequest.getVariableName()));
    }

    @ApiOperation(value="Create batch operation based on filter")
    @PostMapping(value={"/batch-operation"})
    @PreAuthorize(value="hasPermission('write')")
    public BatchOperationEntity createBatchOperation(@RequestBody CreateBatchOperationRequestDto batchOperationRequest) {
        this.validateBatchOperationRequest(batchOperationRequest);
        return this.batchOperationWriter.scheduleBatchOperation(batchOperationRequest);
    }

    @ApiOperation(value="Get process instance by id")
    @GetMapping(value={"/{id}"})
    public ListViewProcessInstanceDto queryProcessInstanceById(@PathVariable @ValidLongId String id) {
        return this.processInstanceReader.getProcessInstanceWithOperationsByKey(Long.valueOf(id));
    }

    @ApiOperation(value="Get incidents by process instance id")
    @GetMapping(value={"/{id}/incidents"})
    public IncidentResponseDto queryIncidentsByProcessInstanceId(@PathVariable @ValidLongId String id) {
        return this.incidentReader.getIncidentsByProcessInstanceId(id);
    }

    @ApiOperation(value="Get sequence flows by process instance id")
    @GetMapping(value={"/{id}/sequence-flows"})
    public List<SequenceFlowDto> querySequenceFlowsByProcessInstanceId(@PathVariable @ValidLongId String id) {
        List sequenceFlows = this.sequenceFlowReader.getSequenceFlowsByProcessInstanceKey(Long.valueOf(id));
        return DtoCreator.create((List)sequenceFlows, SequenceFlowDto.class);
    }

    @ApiOperation(value="Get variables by process instance id and scope id")
    @PostMapping(value={"/{processInstanceId}/variables"})
    public List<VariableDto> getVariables(@PathVariable @ValidLongId String processInstanceId, @RequestBody VariableRequestDto variableRequest) {
        this.validateRequest(variableRequest);
        return this.variableReader.getVariables(processInstanceId, variableRequest);
    }

    @ApiOperation(value="Get flow node states by process instance id")
    @GetMapping(value={"/{processInstanceId}/flow-node-states"})
    public Map<String, FlowNodeStateDto> getFlowNodeStates(@PathVariable @ValidLongId String processInstanceId) {
        return this.flowNodeInstanceReader.getFlowNodeStates(processInstanceId);
    }

    @ApiOperation(value="Get flow node metadata.")
    @PostMapping(value={"/{processInstanceId}/flow-node-metadata"})
    public FlowNodeMetadataDto getFlowNodeMetadata(@PathVariable @ValidLongId String processInstanceId, @RequestBody FlowNodeMetadataRequestDto request) {
        this.validateRequest(request);
        return this.flowNodeInstanceReader.getFlowNodeMetadata(processInstanceId, request);
    }

    private void validateRequest(VariableRequestDto request) {
        if (request.getScopeId() != null) return;
        throw new InvalidRequestException("ScopeId must be specifies in the request.");
    }

    private void validateRequest(FlowNodeMetadataRequestDto request) {
        if (request.getFlowNodeId() == null && request.getFlowNodeType() == null && request.getFlowNodeInstanceId() == null) {
            throw new InvalidRequestException("At least flowNodeId or flowNodeInstanceId must be specifies in the request.");
        }
        if (request.getFlowNodeId() == null) return;
        if (request.getFlowNodeInstanceId() == null) return;
        throw new InvalidRequestException("Only one of flowNodeId or flowNodeInstanceId must be specifies in the request.");
    }

    @ApiOperation(value="Get activity instance statistics")
    @PostMapping(path={"/statistics"})
    public Collection<FlowNodeStatisticsDto> getStatistics(@RequestBody ListViewQueryDto query) {
        List processDefinitionKeys = CollectionUtil.toSafeListOfLongs((Collection)query.getProcessIds());
        String bpmnProcessId = query.getBpmnProcessId();
        Integer processVersion = query.getProcessVersion();
        if ((processDefinitionKeys != null && processDefinitionKeys.size() == 1) != (bpmnProcessId != null && processVersion != null)) return this.activityStatisticsReader.getFlowNodeStatistics(query);
        throw new InvalidRequestException("Exactly one process must be specified in the request (via processIds or bpmnProcessId/version).");
    }

    @ApiOperation(value="Get process instance core statistics (aggregations)")
    @GetMapping(path={"/core-statistics"})
    @Timed(value="operate.query", extraTags={"name", "corestatistics"}, description="How long does it take to retrieve the core statistics.")
    public ProcessInstanceCoreStatisticsDto getCoreStatistics() {
        return this.processInstanceReader.getCoreStatistics();
    }

    @ExceptionHandler(value={ConstraintViolationException.class})
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.status((HttpStatus)HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
