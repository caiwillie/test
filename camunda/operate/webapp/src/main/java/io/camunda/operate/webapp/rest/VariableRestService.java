package io.camunda.operate.webapp.rest;

import io.camunda.operate.webapp.es.reader.VariableReader;
import io.camunda.operate.webapp.rest.dto.VariableDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(
   tags = {"Variables"}
)
@SwaggerDefinition(
   tags = {@Tag(
   name = "Variables",
   description = "Variables"
)}
)
@RestController
@RequestMapping({"/api/variables"})
public class VariableRestService {
   public static final String VARIABLE_URL = "/api/variables";
   @Autowired
   private VariableReader variableReader;

   @ApiOperation("Get full variable by id")
   @GetMapping({"/{id}"})
   public VariableDto getVariable(@PathVariable String id) {
      return this.variableReader.getVariable(id);
   }
}
