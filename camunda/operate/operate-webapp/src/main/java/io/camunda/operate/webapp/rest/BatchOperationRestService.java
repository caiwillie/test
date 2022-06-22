/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.webapp.es.reader.BatchOperationReader
 *  io.camunda.operate.webapp.rest.dto.DtoCreator
 *  io.camunda.operate.webapp.rest.dto.operation.BatchOperationDto
 *  io.camunda.operate.webapp.rest.dto.operation.BatchOperationRequestDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  io.swagger.annotations.Api
 *  io.swagger.annotations.ApiOperation
 *  io.swagger.annotations.SwaggerDefinition
 *  io.swagger.annotations.Tag
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.rest;

import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.webapp.es.reader.BatchOperationReader;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.operation.BatchOperationDto;
import io.camunda.operate.webapp.rest.dto.operation.BatchOperationRequestDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags={"Batch operations"})
@SwaggerDefinition(tags={@Tag(name="Batch operations", description="Batch operations")})
@RestController
@RequestMapping(value={"/api/batch-operations"})
public class BatchOperationRestService {
    public static final String BATCH_OPERATIONS_URL = "/api/batch-operations";
    @Autowired
    private BatchOperationReader batchOperationReader;

    @ApiOperation(value="Query batch operations")
    @PostMapping
    public List<BatchOperationDto> queryBatchOperations(@RequestBody BatchOperationRequestDto batchOperationRequestDto) {
        if (batchOperationRequestDto.getPageSize() == null) {
            throw new InvalidRequestException("pageSize parameter must be provided.");
        }
        if (batchOperationRequestDto.getSearchAfter() != null && batchOperationRequestDto.getSearchBefore() != null) {
            throw new InvalidRequestException("Only one of parameters must be present in request: either searchAfter or searchBefore.");
        }
        if (batchOperationRequestDto.getSearchBefore() != null) {
            if (batchOperationRequestDto.getSearchBefore().length != 2) throw new InvalidRequestException("searchBefore must be an array of two string values.");
            if (!CollectionUtil.allElementsAreOfType(String.class, (Object[])batchOperationRequestDto.getSearchBefore())) {
                throw new InvalidRequestException("searchBefore must be an array of two string values.");
            }
        }
        if (batchOperationRequestDto.getSearchAfter() != null) {
            if (batchOperationRequestDto.getSearchAfter().length != 2) throw new InvalidRequestException("searchAfter must be an array of two string values.");
            if (!CollectionUtil.allElementsAreOfType(String.class, (Object[])batchOperationRequestDto.getSearchAfter())) {
                throw new InvalidRequestException("searchAfter must be an array of two string values.");
            }
        }
        List batchOperations = this.batchOperationReader.getBatchOperations(batchOperationRequestDto);
        return DtoCreator.create((List)batchOperations, BatchOperationDto.class);
    }
}
