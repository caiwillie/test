package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.ErrorType;
import io.camunda.operate.entities.IncidentEntity;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.webapp.es.reader.IncidentReader;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResolveIncidentHandler extends AbstractOperationHandler implements OperationHandler {
   @Autowired
   private IncidentReader incidentReader;
   @Autowired
   private ZeebeClient zeebeClient;

   public void handleWithException(OperationEntity operation) throws Exception {
      if (operation.getIncidentKey() == null) {
         this.failOperation(operation, "Incident key must be defined.");
      } else {
         IncidentEntity incident;
         try {
            incident = this.incidentReader.getIncidentById(operation.getIncidentKey());
         } catch (NotFoundException var4) {
            this.failOperation(operation, "No appropriate incidents found: " + var4.getMessage());
            return;
         }

         if (incident.getErrorType().equals(ErrorType.JOB_NO_RETRIES)) {
            this.zeebeClient.newUpdateRetriesCommand(incident.getJobKey()).retries(1).send().join();
         }

         this.zeebeClient.newResolveIncidentCommand(incident.getKey()).send().join();
         this.markAsSent(operation);
      }
   }

   public Set getTypes() {
      return Set.of(OperationType.RESOLVE_INCIDENT);
   }

   public void setZeebeClient(ZeebeClient zeebeClient) {
      this.zeebeClient = zeebeClient;
   }
}
