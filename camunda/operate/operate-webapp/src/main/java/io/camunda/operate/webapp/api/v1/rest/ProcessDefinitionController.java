/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.dao.ProcessDefinitionDao
 *  io.camunda.operate.webapp.api.v1.entities.Error
 *  io.camunda.operate.webapp.api.v1.entities.ProcessDefinition
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.QueryValidator
 *  io.camunda.operate.webapp.api.v1.entities.Results
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
 *  javax.validation.Valid
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.http.HttpStatus
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.ResponseStatus
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.api.v1.rest;

import io.camunda.operate.webapp.api.v1.dao.ProcessDefinitionDao;
import io.camunda.operate.webapp.api.v1.entities.Error;
import io.camunda.operate.webapp.api.v1.entities.ProcessDefinition;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.QueryValidator;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.rest.ErrorController;
import io.camunda.operate.webapp.api.v1.rest.SearchController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController(value="ProcessDefinitionControllerV1")
@RequestMapping(value={"/v1/process-definitions"})
@Tag(name="ProcessDefinition", description="Process definition API")
@Validated
public class ProcessDefinitionController
extends ErrorController
implements SearchController<ProcessDefinition> {
    public static final String URI = "/v1/process-definitions";
    public static final String AS_XML = "/xml";
    @Autowired
    private ProcessDefinitionDao processDefinitionDao;
    private final QueryValidator<ProcessDefinition> queryValidator = new QueryValidator();

    @Operation(summary="Search process definitions", responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Data invalid", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description="Search examples", content={@Content(examples={@ExampleObject(name="All", value="{}", description="All process instances (default size is 10)"), @ExampleObject(name="Size of returned list", value="{ \"size\": 5 }", description="Search process instances and return list of size 5"), @ExampleObject(name="Sort", value="{ \"sort\": [{\"field\":\"name\",\"order\": \"ASC\"}] }", description="Search process instances and sort by name"), @ExampleObject(name="Sort and size", value="{ \"size\": 5, \"sort\": [{\"field\":\"name\",\"order\": \"DESC\"}] }", description="Search process instances, sort descending by name list size of 5"), @ExampleObject(name="Sort and page", value="{   \"size\": 5,    \"sort\": [{\"field\":\"name\",\"order\": \"ASC\"}],    \"searchAfter\": [      \"Called Process\",      \"2251799813687281\"  ] }", description="Search process instances,sort by name and page results of size 5. \n To get the next page copy the value of 'sortValues' into 'searchAfter' value.\nSort specification should match the searchAfter specification"), @ExampleObject(name="Filter and sort ", value="{   \"filter\": {      \"version\": 1    },    \"size\": 50,    \"sort\": [{\"field\":\"bpmnProcessId\",\"order\": \"ASC\"}]}", description="Filter by version and sort by bpmnProcessId")})})
    public Results<ProcessDefinition> search(@RequestBody Query<ProcessDefinition> query) {
        this.logger.debug("search for query {}", (Object)query);
        this.queryValidator.validate(query, ProcessDefinition.class);
        return this.processDefinitionDao.search(query);
    }

    @Operation(summary="Get process definition by key", tags={"Process"}, responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Requested resource not found", responseCode="404", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    public ProcessDefinition byKey(@Parameter(description="Key of process definition", required=true) @Valid @PathVariable Long key) {
        return this.processDefinitionDao.byKey(key);
    }

    @Operation(summary="Get process definition as XML by key", tags={"Process"}, responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Requested resource not found", responseCode="404", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    @ResponseStatus(value=HttpStatus.OK)
    @GetMapping(value={"/{key}/xml"}, produces={"text/xml"})
    public String xmlByKey(@Parameter(description="Key of process definition", required=true) @Valid @PathVariable Long key) {
        return this.processDefinitionDao.xmlByKey(key);
    }
}
