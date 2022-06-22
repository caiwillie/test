/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.es.reader.DecisionReader
 *  io.camunda.operate.webapp.rest.dto.dmn.DecisionGroupDto
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

import io.camunda.operate.webapp.es.reader.DecisionReader;
import io.camunda.operate.webapp.rest.dto.dmn.DecisionGroupDto;
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

@Api(tags={"Decisions"})
@SwaggerDefinition(tags={@Tag(name="Decisions", description="Decisions")})
@RestController
@RequestMapping(value={"/api/decisions"})
public class DecisionRestService {
    @Autowired
    protected DecisionReader decisionReader;
    public static final String DECISION_URL = "/api/decisions";

    @ApiOperation(value="Get process BPMN XML")
    @GetMapping(path={"/{id}/xml"})
    public String getDecisionDiagram(@PathVariable(value="id") String decisionDefinitionId) {
        return this.decisionReader.getDiagram(decisionDefinitionId);
    }

    @ApiOperation(value="List processes grouped by decisionId")
    @GetMapping(path={"/grouped"})
    public List<DecisionGroupDto> getDecisionsGrouped() {
        Map decisionsGrouped = this.decisionReader.getDecisionsGrouped();
        return DecisionGroupDto.createFrom((Map)decisionsGrouped);
    }
}
