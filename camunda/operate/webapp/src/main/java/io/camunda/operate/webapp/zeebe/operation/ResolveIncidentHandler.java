/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.ErrorType
 *  io.camunda.operate.entities.IncidentEntity
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.webapp.es.reader.IncidentReader
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  io.camunda.operate.webapp.zeebe.operation.AbstractOperationHandler
 *  io.camunda.operate.webapp.zeebe.operation.OperationHandler
 *  io.camunda.zeebe.client.ZeebeClient
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.ErrorType;
import io.camunda.operate.entities.IncidentEntity;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.webapp.es.reader.IncidentReader;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.operate.webapp.zeebe.operation.AbstractOperationHandler;
import io.camunda.operate.webapp.zeebe.operation.OperationHandler;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResolveIncidentHandler
extends AbstractOperationHandler
implements OperationHandler {
    @Autowired
    private IncidentReader incidentReader;
    @Autowired
    private ZeebeClient zeebeClient;

    public void handleWithException(OperationEntity operation) throws Exception {
        IncidentEntity incident;
        if (operation.getIncidentKey() == null) {
            this.failOperation(operation, "Incident key must be defined.");
            return;
        }
        try {
            incident = this.incidentReader.getIncidentById(operation.getIncidentKey());
        }
        catch (NotFoundException ex) {
            this.failOperation(operation, "No appropriate incidents found: " + ex.getMessage());
            return;
        }
        if (incident.getErrorType().equals((Object)ErrorType.JOB_NO_RETRIES)) {
            this.zeebeClient.newUpdateRetriesCommand(incident.getJobKey().longValue()).retries(1).send().join();
        }
        this.zeebeClient.newResolveIncidentCommand(incident.getKey()).send().join();
        this.markAsSent(operation);
    }

    public Set<OperationType> getTypes() {
        return Set.of(OperationType.RESOLVE_INCIDENT);
    }

    public void setZeebeClient(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }
}
