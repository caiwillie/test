package io.camunda.operate.webapp.management;

import io.camunda.operate.es.contract.MetricContract;
import io.camunda.operate.webapp.management.dto.UsageMetricDTO;
import io.camunda.operate.webapp.management.dto.UsageMetricQueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@Component
@RestControllerEndpoint(
   id = "usage-metrics"
)
public class UsageMetricsService {
   @Autowired
   private MetricContract.Reader reader;

   @GetMapping(
      value = {"/process-instances"},
      produces = {"application/json"}
   )
   public UsageMetricDTO retrieveProcessInstanceCount(UsageMetricQueryDTO query) {
      Long total = this.reader.retrieveProcessInstanceCount(query.getStartTime(), query.getEndTime());
      return (new UsageMetricDTO()).setTotal(total);
   }

   @GetMapping(
      value = {"/decision-instances"},
      produces = {"application/json"}
   )
   public UsageMetricDTO retrieveDecisionInstancesCount(UsageMetricQueryDTO query) {
      Long total = this.reader.retrieveDecisionInstanceCount(query.getStartTime(), query.getEndTime());
      return (new UsageMetricDTO()).setTotal(total);
   }
}
