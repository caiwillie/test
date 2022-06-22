/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.dao.VariableDao
 *  io.camunda.operate.webapp.api.v1.entities.Error
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.QueryValidator
 *  io.camunda.operate.webapp.api.v1.entities.Results
 *  io.camunda.operate.webapp.api.v1.entities.Variable
 *  io.camunda.operate.webapp.api.v1.rest.ErrorController
 *  io.camunda.operate.webapp.api.v1.rest.SearchController
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.Parameter
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.ExampleObject
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.parameters.RequestBody
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.api.v1.rest;

import io.camunda.operate.webapp.api.v1.dao.VariableDao;
import io.camunda.operate.webapp.api.v1.entities.Error;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.QueryValidator;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.entities.Variable;
import io.camunda.operate.webapp.api.v1.rest.ErrorController;
import io.camunda.operate.webapp.api.v1.rest.SearchController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value="VariableControllerV1")
@RequestMapping(value={"/v1/variables"})
@Tag(name="Variable", description="Variable API")
@Validated
public class VariableController
extends ErrorController
implements SearchController<Variable> {
    public static final String URI = "/v1/variables";
    public static final String BY_PROCESS_INSTANCE_KEY = "/process-instance/{key}";
    @Autowired
    private VariableDao variableDao;
    private final QueryValidator<Variable> queryValidator = new QueryValidator();

    @Operation(summary="Search variables for process instances", responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Data invalid", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description="Search variables", content={@Content(examples={@ExampleObject(name="All", value="{}", description="Returns all variables (default return list size is 10)"), @ExampleObject(name="Size", value="{ \"size\": 20 }", description="Returns 20 variables "), @ExampleObject(name="Filter and sort", value="{  \"filter\": {    \"processInstanceKey\": \"9007199254741196\"  },  \"sort\": [{\"field\":\"name\",\"order\":\"ASC\"}]}", description="Returns all variables with 'processInstanceKey' '9007199254741196' sorted ascending by name"), @ExampleObject(name="Paging", value="{  \"filter\": {    \"processInstanceKey\": \"9007199254741196\"  },  \"sort\": [{\"field\":\"name\",\"order\":\"ASC\"}],  \"searchAfter\":[    \"small\",    9007199254741200  ]}", description="Returns next variables for 'processInstanceKey' ascending by 'name'. (Copy value of 'sortValues' field of previous results) ")})})
    public Results<Variable> search(@RequestBody Query<Variable> query) {
        this.logger.debug("search for query {}", (Object)query);
        this.queryValidator.validate(query, Variable.class);
        return this.variableDao.search(query);
    }

    @Operation(summary="Get variable by key", responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Requested resource not found", responseCode="404", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    public Variable byKey(@Parameter(description="Key of variable", required=true) @PathVariable Long key) {
        return this.variableDao.byKey(key);
    }
}
