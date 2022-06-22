/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.dao.FlowNodeInstanceDao
 *  io.camunda.operate.webapp.api.v1.entities.Error
 *  io.camunda.operate.webapp.api.v1.entities.FlowNodeInstance
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
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.api.v1.rest;

import io.camunda.operate.webapp.api.v1.dao.FlowNodeInstanceDao;
import io.camunda.operate.webapp.api.v1.entities.Error;
import io.camunda.operate.webapp.api.v1.entities.FlowNodeInstance;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value="FlowNodeInstanceControllerV1")
@RequestMapping(value={"/v1/flownode-instances"})
@Tag(name="Flownode-instance", description="Flownode Instances API")
@Validated
public class FlowNodeInstanceController
extends ErrorController
implements SearchController<FlowNodeInstance> {
    public static final String URI = "/v1/flownode-instances";
    @Autowired
    private FlowNodeInstanceDao flowNodeInstanceDao;
    private final QueryValidator<FlowNodeInstance> queryValidator = new QueryValidator();

    @Operation(summary="Search flownode-instances", responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Data invalid", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description="Search flownode-instances", content={@Content(examples={@ExampleObject(name="All", value="{}", description="Returns all flownode instances (default return list size is 10)."), @ExampleObject(name="Return 20 items", value="{ \"size\": 20 }", description="Returns max 20 incidents."), @ExampleObject(name="Sort by field", value="{ \"sort\": [{\"field\":\"endDate\",\"order\": \"DESC\"}] }", description="Returns flownode instances sorted descending by 'endDate'"), @ExampleObject(name="Filter by field", value="{ \"filter\": { \"incident\": true} }", description="Returns flownode instances filtered by 'incident'."), @ExampleObject(name="Filter and sort", value="{  \"filter\": {    \"incident\": true  },  \"sort\": [    {      \"field\": \"startDate\",      \"order\": \"DESC\"    }  ]}", description="Filter by 'incident' , sorted descending by 'startDate'."), @ExampleObject(name="Page by key", value="{ \"searchAfter\":  [    2251799813687785  ]}", description="Returns paged by using previous returned 'sortValues' value (array). Choose an existing key from previous searches to try this."), @ExampleObject(name="Filter, sort and page", value="{  \"filter\": {     \"incident\": true  },  \"sort\":[{\"field\":\"startDate\",\"order\":\"ASC\"}],\"searchAfter\":[    1646904085499,    9007199254743288  ]}", description="Returns flownode instances filtered by 'incident' , sorted ascending by 'startDate' and paged from previous 'sortValues' value.")})})
    public Results<FlowNodeInstance> search(@RequestBody Query<FlowNodeInstance> query) {
        this.logger.debug("search for query {}", (Object)query);
        this.queryValidator.validate(query, FlowNodeInstance.class);
        return this.flowNodeInstanceDao.search(query);
    }

    @Operation(summary="Get flownode instance by key", responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Requested resource not found", responseCode="404", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    public FlowNodeInstance byKey(@Parameter(description="Key of flownode instance", required=true) @PathVariable Long key) {
        return this.flowNodeInstanceDao.byKey(key);
    }
}
