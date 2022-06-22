/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.es.reader.IncidentStatisticsReader
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentsByErrorMsgStatisticsDto
 *  io.camunda.operate.webapp.rest.dto.incidents.IncidentsByProcessGroupStatisticsDto
 *  io.swagger.annotations.Api
 *  io.swagger.annotations.ApiOperation
 *  io.swagger.annotations.SwaggerDefinition
 *  io.swagger.annotations.Tag
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.rest;

import io.camunda.operate.webapp.es.reader.IncidentStatisticsReader;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentsByErrorMsgStatisticsDto;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentsByProcessGroupStatisticsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags={"Incidents statistics"})
@SwaggerDefinition(tags={@Tag(name="Incidents statistics", description="Incidents statistics")})
@RestController
@RequestMapping(value={"/api/incidents"})
public class IncidentRestService {
    public static final String INCIDENT_URL = "/api/incidents";
    @Autowired
    private IncidentStatisticsReader incidentStatisticsReader;

    @ApiOperation(value="Get incident statistics for processes")
    @GetMapping(value={"/byProcess"})
    public Collection<IncidentsByProcessGroupStatisticsDto> getProcessAndIncidentsStatistics() {
        return this.incidentStatisticsReader.getProcessAndIncidentsStatistics();
    }

    @ApiOperation(value="Get incident statistics by error message")
    @GetMapping(value={"/byError"})
    public Collection<IncidentsByErrorMsgStatisticsDto> getIncidentStatisticsByError() {
        return this.incidentStatisticsReader.getIncidentStatisticsByError();
    }
}
