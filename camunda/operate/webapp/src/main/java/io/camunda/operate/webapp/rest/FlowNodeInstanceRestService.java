package io.camunda.operate.webapp.rest;

import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.webapp.es.reader.FlowNodeInstanceReader;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceQueryDto;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceRequestDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(
   tags = {"Flow node instances"}
)
@SwaggerDefinition(
   tags = {@Tag(
   name = "Flow node instances",
   description = "Flow node instances"
)}
)
@RestController
@RequestMapping({"/api/flow-node-instances"})
public class FlowNodeInstanceRestService {
   public static final String FLOW_NODE_INSTANCE_URL = "/api/flow-node-instances";
   @Autowired
   private FlowNodeInstanceReader flowNodeInstanceReader;

   @ApiOperation("Query flow node instance tree. Returns map treePath <-> list of children.")
   @PostMapping
   public Map queryFlowNodeInstanceTree(@RequestBody FlowNodeInstanceRequestDto request) {
      this.validateRequest(request);
      return this.flowNodeInstanceReader.getFlowNodeInstances(request);
   }

   private void validateRequest(FlowNodeInstanceRequestDto request) {
      if (request.getQueries() != null && request.getQueries().size() != 0) {
         Iterator var2 = request.getQueries().iterator();

         FlowNodeInstanceQueryDto query;
         do {
            if (!var2.hasNext()) {
               return;
            }

            query = (FlowNodeInstanceQueryDto)var2.next();
            if (query == null || query.getProcessInstanceId() == null || query.getTreePath() == null) {
               throw new InvalidRequestException("Process instance id and tree path must be provided when requesting for flow node instance tree.");
            }
         } while(CollectionUtil.countNonNullObjects(new Object[]{query.getSearchAfter(), query.getSearchAfterOrEqual(), query.getSearchBefore(), query.getSearchBeforeOrEqual()}) <= 1L);

         throw new InvalidRequestException("Only one of [searchAfter, searchAfterOrEqual, searchBefore, searchBeforeOrEqual] must be present in request.");
      } else {
         throw new InvalidRequestException("At least one query must be provided when requesting for flow node instance tree.");
      }
   }
}
