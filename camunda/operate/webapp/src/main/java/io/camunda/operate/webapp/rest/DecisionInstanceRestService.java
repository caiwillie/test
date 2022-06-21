package io.camunda.operate.webapp.rest;

import io.camunda.operate.webapp.es.reader.DecisionInstanceReader;
import io.camunda.operate.webapp.rest.dto.dmn.DecisionInstanceDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListRequestDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListResponseDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(
   tags = {"Decision instances"}
)
@SwaggerDefinition(
   tags = {@Tag(
   name = "Decision instances",
   description = "Decision instances"
)}
)
@RestController
@RequestMapping({"/api/decision-instances"})
@Validated
public class DecisionInstanceRestService {
   public static final String DECISION_INSTANCE_URL = "/api/decision-instances";
   @Autowired
   private DecisionInstanceReader decisionInstanceReader;

   @ApiOperation("Query decision instances by different parameters")
   @PostMapping
   public DecisionInstanceListResponseDto queryDecisionInstances(@RequestBody DecisionInstanceListRequestDto decisionInstanceRequest) {
      if (decisionInstanceRequest.getQuery() == null) {
         throw new InvalidRequestException("Query must be provided.");
      } else {
         return this.decisionInstanceReader.queryDecisionInstances(decisionInstanceRequest);
      }
   }

   @ApiOperation("Get decision instance by id")
   @GetMapping({"/{decisionInstanceId}"})
   public DecisionInstanceDto queryProcessInstanceById(@PathVariable String decisionInstanceId) {
      return this.decisionInstanceReader.getDecisionInstance(decisionInstanceId);
   }

   @ApiOperation("Get DRD data for decision instance")
   @GetMapping({"/{decisionInstanceId}/drd-data"})
   public Map queryProcessInstanceDRDData(@PathVariable String decisionInstanceId) {
      Map result = this.decisionInstanceReader.getDecisionInstanceDRDData(decisionInstanceId);
      if (result.isEmpty()) {
         throw new NotFoundException("Decision instance nor found: " + decisionInstanceId);
      } else {
         return result;
      }
   }
}
