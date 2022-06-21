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

@Api(
   tags = {"Processes"}
)
@SwaggerDefinition(
   tags = {@Tag(
   name = "Processes",
   description = "Processes"
)}
)
@RestController
@RequestMapping({"/api/processes"})
public class ProcessRestService {
   @Autowired
   protected ProcessReader processReader;
   @Autowired
   protected ProcessInstanceReader processInstanceReader;
   public static final String PROCESS_URL = "/api/processes";

   @ApiOperation("Get process BPMN XML")
   @GetMapping(
      path = {"/{id}/xml"}
   )
   public String getProcessDiagram(@PathVariable("id") String processId) {
      return this.processReader.getDiagram(Long.valueOf(processId));
   }

   @ApiOperation("Get process by id")
   @GetMapping(
      path = {"/{id}"}
   )
   public ProcessDto getProcess(@PathVariable("id") String processId) {
      ProcessEntity processEntity = this.processReader.getProcess(Long.valueOf(processId));
      return (ProcessDto)DtoCreator.create((Object)processEntity, ProcessDto.class);
   }

   @ApiOperation("List processes grouped by bpmnProcessId")
   @GetMapping(
      path = {"/grouped"}
   )
   public List getProcessesGrouped() {
      Map processesGrouped = this.processReader.getProcessesGrouped();
      return ProcessGroupDto.createFrom(processesGrouped);
   }
}
