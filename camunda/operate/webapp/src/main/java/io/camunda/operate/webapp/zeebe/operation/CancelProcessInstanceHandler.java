/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity
 *  io.camunda.operate.entities.listview.ProcessInstanceState
 *  io.camunda.operate.webapp.es.reader.ProcessInstanceReader
 *  io.camunda.operate.webapp.zeebe.operation.AbstractOperationHandler
 *  io.camunda.operate.webapp.zeebe.operation.OperationHandler
 *  io.camunda.zeebe.client.ZeebeClient
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.webapp.es.reader.ProcessInstanceReader;
import io.camunda.operate.webapp.zeebe.operation.AbstractOperationHandler;
import io.camunda.operate.webapp.zeebe.operation.OperationHandler;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelProcessInstanceHandler
extends AbstractOperationHandler
implements OperationHandler {
    @Autowired
    private ProcessInstanceReader processInstanceReader;
    @Autowired
    private ZeebeClient zeebeClient;

    public void handleWithException(OperationEntity operation) throws Exception {
        if (operation.getProcessInstanceKey() == null) {
            this.failOperation(operation, "No process instance id is provided.");
            return;
        }
        ProcessInstanceForListViewEntity processInstance = this.processInstanceReader.getProcessInstanceByKey(operation.getProcessInstanceKey());
        if (!processInstance.getState().equals((Object)ProcessInstanceState.ACTIVE)) {
            this.failOperation(operation, String.format("Unable to cancel %s process instance. Instance must be in ACTIVE or INCIDENT state.", processInstance.getState()));
            return;
        }
        this.zeebeClient.newCancelInstanceCommand(processInstance.getKey()).send().join();
        this.markAsSent(operation);
    }

    public Set<OperationType> getTypes() {
        return Set.of(OperationType.CANCEL_PROCESS_INSTANCE);
    }

    public void setZeebeClient(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }
}
