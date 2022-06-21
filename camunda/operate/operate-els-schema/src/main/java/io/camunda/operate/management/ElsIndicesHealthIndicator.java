package io.camunda.operate.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("elsIndicesCheck")
public class ElsIndicesHealthIndicator implements HealthIndicator {
   private static Logger logger = LoggerFactory.getLogger(ElsIndicesHealthIndicator.class);
   @Autowired
   private ElsIndicesCheck elsIndicesCheck;

   public Health health() {
      logger.debug("ELS indices check is called");
      return this.elsIndicesCheck.indicesArePresent() ? Health.up().build() : Health.down().build();
   }

   public Health getHealth(boolean includeDetails) {
      return this.health();
   }
}
