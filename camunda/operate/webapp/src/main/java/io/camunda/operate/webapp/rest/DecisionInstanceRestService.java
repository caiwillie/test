/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.es.reader.DecisionInstanceReader
 *  io.camunda.operate.webapp.rest.dto.dmn.DRDDataEntryDto
 *  io.camunda.operate.webapp.rest.dto.dmn.DecisionInstanceDto
 *  io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListRequestDto
 *  io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListResponseDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  io.swagger.annotations.Api
 *  io.swagger.annotations.ApiOperation
 *  io.swagger.annotations.SwaggerDefinition
 *  io.swagger.annotations.Tag
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.rest;

import io.camunda.operate.webapp.es.reader.DecisionInstanceReader;
import io.camunda.operate.webapp.rest.dto.dmn.DRDDataEntryDto;
import io.camunda.operate.webapp.rest.dto.dmn.DecisionInstanceDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListRequestDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListResponseDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags={"Decision instances"})
@SwaggerDefinition(tags={@Tag(name="Decision instances", description="Decision instances")})
@RestController
@RequestMapping(value={"/api/decision-instances"})
@Validated
public class DecisionInstanceRestService {
    public static final String DECISION_INSTANCE_URL = "/api/decision-instances";
    @Autowired
    private DecisionInstanceReader decisionInstanceReader;

    @ApiOperation(value="Query decision instances by different parameters")
    @PostMapping
    public DecisionInstanceListResponseDto queryDecisionInstances(@RequestBody DecisionInstanceListRequestDto decisionInstanceRequest) {
        if (decisionInstanceRequest.getQuery() != null) return this.decisionInstanceReader.queryDecisionInstances(decisionInstanceRequest);
        throw new InvalidRequestException("Query must be provided.");
    }

    @ApiOperation(value="Get decision instance by id")
    @GetMapping(value={"/{decisionInstanceId}"})
    public DecisionInstanceDto queryProcessInstanceById(@PathVariable String decisionInstanceId) {
        return this.decisionInstanceReader.getDecisionInstance(decisionInstanceId);
    }

    @ApiOperation(value="Get DRD data for decision instance")
    @GetMapping(value={"/{decisionInstanceId}/drd-data"})
    public Map<String, List<DRDDataEntryDto>> queryProcessInstanceDRDData(@PathVariable String decisionInstanceId) {
        Map result = this.decisionInstanceReader.getDecisionInstanceDRDData(decisionInstanceId);
        if (!result.isEmpty()) return result;
        throw new NotFoundException("Decision instance nor found: " + decisionInstanceId);
    }
}
