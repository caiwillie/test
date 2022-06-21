package io.camunda.operate.webapp.api.v1.rest;

import io.camunda.operate.webapp.api.v1.dao.ProcessInstanceDao;
import io.camunda.operate.webapp.api.v1.entities.ChangeStatus;
import io.camunda.operate.webapp.api.v1.entities.Error;
import io.camunda.operate.webapp.api.v1.entities.ProcessInstance;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.QueryValidator;
import io.camunda.operate.webapp.api.v1.entities.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController("ProcessInstanceControllerV1")
@RequestMapping({"/v1/process-instances"})
@Tag(
   name = "ProcessInstance",
   description = "Process instance API"
)
@Validated
public class ProcessInstanceController extends ErrorController implements SearchController {
   public static final String URI = "/v1/process-instances";
   @Autowired
   private ProcessInstanceDao processInstanceDao;
   private final QueryValidator queryValidator = new QueryValidator();

   @Operation(
      summary = "Search process instances",
      responses = {@ApiResponse(
   description = "Success",
   responseCode = "200"
), @ApiResponse(
   description = "API application error",
   responseCode = "500",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
), @ApiResponse(
   description = "Invalid request",
   responseCode = "400",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
), @ApiResponse(
   description = "Data invalid",
   responseCode = "400",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
)}
   )
   @RequestBody(
      description = "Search process instances",
      content = {@Content(
   examples = {@ExampleObject(
   name = "All",
   value = "{}",
   description = "Returns all process instances (default return list size is 10)"
), @ExampleObject(
   name = "Sorted by field",
   value = "{  \"sort\": [{\"field\":\"bpmnProcessId\",\"order\": \"ASC\"}] }",
   description = "Returns process instances sorted ascending by bpmnProcessId"
), @ExampleObject(
   name = "Sorted and paged with size",
   value = "{  \"size\": 3,    \"sort\": [{\"field\":\"bpmnProcessId\",\"order\": \"ASC\"}],    \"searchAfter\":[    \"bigVarProcess\",    6755399441055870  ]}",
   description = "Returns max 3 process instances after 'bigVarProcess' and key 6755399441055870 sorted ascending by bpmnProcessId \nTo get the next page copy the value of 'sortValues' into 'searchAfter' value.\nSort specification should match the searchAfter specification"
), @ExampleObject(
   name = "Filtered and sorted",
   value = "{  \"filter\": {      \"processVersion\": 2    },    \"size\": 50,    \"sort\": [{\"field\":\"bpmnProcessId\",\"order\": \"ASC\"}]}",
   description = "Returns max 50 process instances, filtered by processVersion of 2 sorted ascending by bpmnProcessId"
)}
)}
   )
   public Results search(@org.springframework.web.bind.annotation.RequestBody Query query) {
      this.logger.debug("search for query {}", query);
      this.queryValidator.validate(query, ProcessInstance.class);
      return this.processInstanceDao.search(query);
   }

   @Operation(
      summary = "Get process instance by key",
      responses = {@ApiResponse(
   description = "Success",
   responseCode = "200"
), @ApiResponse(
   description = "API application error",
   responseCode = "500",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
), @ApiResponse(
   description = "Invalid request",
   responseCode = "400",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
), @ApiResponse(
   description = "Requested resource not found",
   responseCode = "404",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
)}
   )
   public ProcessInstance byKey(@Parameter(description = "Key of process instance",required = true) @PathVariable Long key) {
      return this.processInstanceDao.byKey(key);
   }

   @Operation(
      summary = "Delete process instance and all dependant data by key",
      responses = {@ApiResponse(
   description = "Success",
   responseCode = "200"
), @ApiResponse(
   description = "API application error",
   responseCode = "500",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
), @ApiResponse(
   description = "Invalid request",
   responseCode = "400",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
), @ApiResponse(
   description = "Requested resource not found",
   responseCode = "404",
   content = {@Content(
   mediaType = "application/problem+json",
   schema = @Schema(
   implementation = Error.class
)
)}
)}
   )
   @ResponseStatus(HttpStatus.OK)
   @DeleteMapping(
      value = {"/{key}"},
      produces = {"application/json"}
   )
   public ChangeStatus delete(@Parameter(description = "Key of process instance",required = true) @PathVariable @Valid Long key) {
      return this.processInstanceDao.delete(key);
   }
}
