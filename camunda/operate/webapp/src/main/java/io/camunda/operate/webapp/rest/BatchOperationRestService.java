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

@Api(
   tags = {"Batch operations"}
)
@SwaggerDefinition(
   tags = {@Tag(
   name = "Batch operations",
   description = "Batch operations"
)}
)
@RestController
@RequestMapping({"/api/batch-operations"})
public class BatchOperationRestService {
   public static final String BATCH_OPERATIONS_URL = "/api/batch-operations";
   @Autowired
   private BatchOperationReader batchOperationReader;

   @ApiOperation("Query batch operations")
   @PostMapping
   public List queryBatchOperations(@RequestBody BatchOperationRequestDto batchOperationRequestDto) {
      if (batchOperationRequestDto.getPageSize() == null) {
         throw new InvalidRequestException("pageSize parameter must be provided.");
      } else if (batchOperationRequestDto.getSearchAfter() != null && batchOperationRequestDto.getSearchBefore() != null) {
         throw new InvalidRequestException("Only one of parameters must be present in request: either searchAfter or searchBefore.");
      } else if (batchOperationRequestDto.getSearchBefore() == null || batchOperationRequestDto.getSearchBefore().length == 2 && CollectionUtil.allElementsAreOfType(String.class, batchOperationRequestDto.getSearchBefore())) {
         if (batchOperationRequestDto.getSearchAfter() == null || batchOperationRequestDto.getSearchAfter().length == 2 && CollectionUtil.allElementsAreOfType(String.class, batchOperationRequestDto.getSearchAfter())) {
            List batchOperations = this.batchOperationReader.getBatchOperations(batchOperationRequestDto);
            return DtoCreator.create(batchOperations, BatchOperationDto.class);
         } else {
            throw new InvalidRequestException("searchAfter must be an array of two string values.");
         }
      } else {
         throw new InvalidRequestException("searchBefore must be an array of two string values.");
      }
   }
}
