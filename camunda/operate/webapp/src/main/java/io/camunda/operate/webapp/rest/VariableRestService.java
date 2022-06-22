/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.es.reader.VariableReader
 *  io.camunda.operate.webapp.rest.dto.VariableDto
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

@Api(tags={"Variables"})
@SwaggerDefinition(tags={@Tag(name="Variables", description="Variables")})
@RestController
@RequestMapping(value={"/api/variables"})
public class VariableRestService {
    public static final String VARIABLE_URL = "/api/variables";
    @Autowired
    private VariableReader variableReader;

    @ApiOperation(value="Get full variable by id")
    @GetMapping(value={"/{id}"})
    public VariableDto getVariable(@PathVariable String id) {
        return this.variableReader.getVariable(id);
    }
}
