/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.dao.IncidentDao
 *  io.camunda.operate.webapp.api.v1.entities.Error
 *  io.camunda.operate.webapp.api.v1.entities.Incident
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.Query$Sort
 *  io.camunda.operate.webapp.api.v1.entities.QueryValidator
 *  io.camunda.operate.webapp.api.v1.entities.QueryValidator$CustomQueryValidator
 *  io.camunda.operate.webapp.api.v1.entities.Results
 *  io.camunda.operate.webapp.api.v1.exceptions.ValidationException
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

import io.camunda.operate.webapp.api.v1.dao.IncidentDao;
import io.camunda.operate.webapp.api.v1.entities.Error;
import io.camunda.operate.webapp.api.v1.entities.Incident;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.QueryValidator;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.camunda.operate.webapp.api.v1.exceptions.ValidationException;
import io.camunda.operate.webapp.api.v1.rest.ErrorController;
import io.camunda.operate.webapp.api.v1.rest.SearchController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value="IncidentControllerV1")
@RequestMapping(value={"/v1/incidents"})
@Tag(name="Incident", description="Incident API")
@Validated
public class IncidentController
extends ErrorController
implements SearchController<Incident> {
    public static final String URI = "/v1/incidents";
    @Autowired
    private IncidentDao incidentDao;
    private final QueryValidator<Incident> queryValidator = new QueryValidator();
    private static final QueryValidator.CustomQueryValidator<Incident> messageSortValidator = query -> {
        Query.Sort sort;
        List sorts = query.getSort();
        if (sorts == null) return;
        Iterator iterator = sorts.iterator();
        do {
            if (!iterator.hasNext()) return;
        } while (!(sort = (Query.Sort)iterator.next()).getField().equals("message"));
        throw new ValidationException(String.format("Field '%s' can't be used as sort field", "message"));
    };

    @Operation(summary="Search incidents", responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Data invalid", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description="Search incidents", content={@Content(examples={@ExampleObject(name="All", value="{}", description="Returns all incidents (default return list size is 10)."), @ExampleObject(name="Return 20 items", value="{ \"size\": 20 }", description="Returns max 20 incidents."), @ExampleObject(name="Sort by field", value="{ \"sort\": [{\"field\":\"creationTime\",\"order\": \"DESC\"}] }", description="Returns incidents sorted descending by 'creationTime'"), @ExampleObject(name="Filter by field", value="{ \"filter\": { \"type\":\"UNHANDLED_ERROR_EVENT\"} }", description="Returns incidents filtered by 'type'. Field 'message' can't be used for filter and sort"), @ExampleObject(name="Filter and sort", value="{  \"filter\": {     \"type\":\"CALLED_ELEMENT_ERROR\",     \"processDefinitionKey\":\"2251799813686167\"  },  \"sort\":[{\"field\":\"creationTime\",\"order\":\"DESC\"}]}", description="Filter by 'type' and 'processDefinitionKey', sorted descending by 'creationTime'."), @ExampleObject(name="Page by key", value="{ \"searchAfter\":  [    2251799813687785  ]}", description="Returns paged by using previous returned 'sortValues' value (array)."), @ExampleObject(name="Filter, sort and page", value="{  \"filter\": {     \"type\":\"CALLED_ELEMENT_ERROR\",     \"processDefinitionKey\":\"2251799813686167\"  },  \"sort\":[{\"field\":\"creationTime\",\"order\":\"DESC\"}],\"searchAfter\":[    1646904085499,    9007199254743288  ]}", description="Returns incidents filtered by 'type' and 'processDefinitionKey', sorted descending by 'creationTime' and paged from previous 'sortValues' value.")})})
    public Results<Incident> search(@RequestBody Query<Incident> query) {
        this.logger.debug("search for query {}", (Object)query);
        this.queryValidator.validate(query, Incident.class, messageSortValidator);
        return this.incidentDao.search(query);
    }

    @Operation(summary="Get incident by key", responses={@ApiResponse(description="Success", responseCode="200"), @ApiResponse(description="API application error", responseCode="500", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Invalid request", responseCode="400", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))}), @ApiResponse(description="Requested resource not found", responseCode="404", content={@Content(mediaType="application/problem+json", schema=@Schema(implementation=Error.class))})})
    public Incident byKey(@Parameter(description="Key of process instance", required=true) @PathVariable Long key) {
        return this.incidentDao.byKey(key);
    }
}
