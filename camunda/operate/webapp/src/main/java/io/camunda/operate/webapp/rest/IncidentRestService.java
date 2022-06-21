package io.camunda.operate.webapp.rest;

import io.camunda.operate.webapp.es.reader.IncidentStatisticsReader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(
   tags = {"Incidents statistics"}
)
@SwaggerDefinition(
   tags = {@Tag(
   name = "Incidents statistics",
   description = "Incidents statistics"
)}
)
@RestController
@RequestMapping({"/api/incidents"})
public class IncidentRestService {
   public static final String INCIDENT_URL = "/api/incidents";
   @Autowired
   private IncidentStatisticsReader incidentStatisticsReader;

   @ApiOperation("Get incident statistics for processes")
   @GetMapping({"/byProcess"})
   public Collection getProcessAndIncidentsStatistics() {
      return this.incidentStatisticsReader.getProcessAndIncidentsStatistics();
   }

   @ApiOperation("Get incident statistics by error message")
   @GetMapping({"/byError"})
   public Collection getIncidentStatisticsByError() {
      return this.incidentStatisticsReader.getIncidentStatisticsByError();
   }
}
