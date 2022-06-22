/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.es.reader.OperationReader
 *  io.camunda.operate.webapp.rest.dto.OperationDto
 *  io.swagger.annotations.Api
 *  io.swagger.annotations.ApiOperation
 *  io.swagger.annotations.SwaggerDefinition
 *  io.swagger.annotations.Tag
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.rest;

import io.camunda.operate.webapp.es.reader.OperationReader;
import io.camunda.operate.webapp.rest.dto.OperationDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags={"Operations"})
@SwaggerDefinition(tags={@Tag(name="Operations", description="Operations")})
@RestController
@RequestMapping(value={"/api/operations"})
public class OperationRestService {
    public static final String OPERATION_URL = "/api/operations";
    @Autowired
    private OperationReader operationReader;

    @ApiOperation(value="Get single operation")
    @GetMapping
    public List<OperationDto> getOperation(@RequestParam String batchOperationId) {
        return this.operationReader.getOperationsByBatchOperationId(batchOperationId);
    }
}
