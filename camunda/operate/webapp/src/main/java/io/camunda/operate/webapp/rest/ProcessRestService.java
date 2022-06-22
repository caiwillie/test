/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.ProcessEntity
 *  io.camunda.operate.webapp.es.reader.ProcessInstanceReader
 *  io.camunda.operate.webapp.es.reader.ProcessReader
 *  io.camunda.operate.webapp.rest.dto.DtoCreator
 *  io.camunda.operate.webapp.rest.dto.ProcessDto
 *  io.camunda.operate.webapp.rest.dto.ProcessGroupDto
 *  io.swagger.annotations.Api
 *  io.swagger.annotations.ApiOperation
 *  io.swagger.annotations.SwaggerDefinition
 *  io.swagger.annotations.Tag
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.rest;

import io.camunda.operate.entities.ProcessEntity;
import io.camunda.operate.webapp.es.reader.ProcessInstanceReader;
import io.camunda.operate.webapp.es.reader.ProcessReader;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.ProcessDto;
import io.camunda.operate.webapp.rest.dto.ProcessGroupDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags={"Processes"})
@SwaggerDefinition(tags={@Tag(name="Processes", description="Processes")})
@RestController
@RequestMapping(value={"/api/processes"})
public class ProcessRestService {
    @Autowired
    protected ProcessReader processReader;
    @Autowired
    protected ProcessInstanceReader processInstanceReader;
    public static final String PROCESS_URL = "/api/processes";

    @ApiOperation(value="Get process BPMN XML")
    @GetMapping(path={"/{id}/xml"})
    public String getProcessDiagram(@PathVariable(value="id") String processId) {
        return this.processReader.getDiagram(Long.valueOf(processId));
    }

    @ApiOperation(value="Get process by id")
    @GetMapping(path={"/{id}"})
    public ProcessDto getProcess(@PathVariable(value="id") String processId) {
        ProcessEntity processEntity = this.processReader.getProcess(Long.valueOf(processId));
        return (ProcessDto)DtoCreator.create(processEntity, ProcessDto.class);
    }

    @ApiOperation(value="List processes grouped by bpmnProcessId")
    @GetMapping(path={"/grouped"})
    public List<ProcessGroupDto> getProcessesGrouped() {
        Map processesGrouped = this.processReader.getProcessesGrouped();
        return ProcessGroupDto.createFrom((Map)processesGrouped);
    }
}
