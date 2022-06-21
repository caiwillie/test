package io.camunda.operate.webapp.rest;

import io.camunda.operate.webapp.es.reader.OperationReader;
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

@Api(
   tags = {"Operations"}
)
@SwaggerDefinition(
   tags = {@Tag(
   name = "Operations",
   description = "Operations"
)}
)
@RestController
@RequestMapping({"/api/operations"})
public class OperationRestService {
   public static final String OPERATION_URL = "/api/operations";
   @Autowired
   private OperationReader operationReader;

   @ApiOperation("Get single operation")
   @GetMapping
   public List getOperation(@RequestParam String batchOperationId) {
      return this.operationReader.getOperationsByBatchOperationId(batchOperationId);
   }
}
