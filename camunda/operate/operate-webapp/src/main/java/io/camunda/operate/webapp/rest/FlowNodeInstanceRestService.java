/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.webapp.es.reader.FlowNodeInstanceReader
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceQueryDto
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceRequestDto
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceResponseDto
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
import io.camunda.operate.webapp.es.reader.FlowNodeInstanceReader;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceQueryDto;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceRequestDto;
import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceResponseDto;
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

@Api(tags={"Flow node instances"})
@SwaggerDefinition(tags={@Tag(name="Flow node instances", description="Flow node instances")})
@RestController
@RequestMapping(value={"/api/flow-node-instances"})
public class FlowNodeInstanceRestService {
    public static final String FLOW_NODE_INSTANCE_URL = "/api/flow-node-instances";
    @Autowired
    private FlowNodeInstanceReader flowNodeInstanceReader;

    @ApiOperation(value="Query flow node instance tree. Returns map treePath <-> list of children.")
    @PostMapping
    public Map<String, FlowNodeInstanceResponseDto> queryFlowNodeInstanceTree(@RequestBody FlowNodeInstanceRequestDto request) {
        this.validateRequest(request);
        return this.flowNodeInstanceReader.getFlowNodeInstances(request);
    }

    private void validateRequest(FlowNodeInstanceRequestDto request) {
        FlowNodeInstanceQueryDto query;
        if (request.getQueries() == null) throw new InvalidRequestException("At least one query must be provided when requesting for flow node instance tree.");
        if (request.getQueries().size() == 0) {
            throw new InvalidRequestException("At least one query must be provided when requesting for flow node instance tree.");
        }
        Iterator iterator = request.getQueries().iterator();
        do {
            if (!iterator.hasNext()) return;
            query = (FlowNodeInstanceQueryDto)iterator.next();
            if (query == null) throw new InvalidRequestException("Process instance id and tree path must be provided when requesting for flow node instance tree.");
            if (query.getProcessInstanceId() == null) throw new InvalidRequestException("Process instance id and tree path must be provided when requesting for flow node instance tree.");
            if (query.getTreePath() != null) continue;
            throw new InvalidRequestException("Process instance id and tree path must be provided when requesting for flow node instance tree.");
        } while (CollectionUtil.countNonNullObjects((Object[])new Object[]{query.getSearchAfter(), query.getSearchAfterOrEqual(), query.getSearchBefore(), query.getSearchBeforeOrEqual()}) <= 1L);
        throw new InvalidRequestException("Only one of [searchAfter, searchAfterOrEqual, searchBefore, searchBeforeOrEqual] must be present in request.");
    }
}
