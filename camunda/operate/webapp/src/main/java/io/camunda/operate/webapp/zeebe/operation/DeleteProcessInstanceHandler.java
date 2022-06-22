/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity
 *  io.camunda.operate.exceptions.PersistenceException
 *  io.camunda.operate.util.OperationsManager
 *  io.camunda.operate.webapp.es.reader.ProcessInstanceReader
 *  io.camunda.operate.webapp.es.writer.ProcessInstanceWriter
 *  io.camunda.operate.webapp.zeebe.operation.AbstractOperationHandler
 *  io.camunda.operate.webapp.zeebe.operation.OperationHandler
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.util.OperationsManager;
import io.camunda.operate.webapp.es.reader.ProcessInstanceReader;
import io.camunda.operate.webapp.es.writer.ProcessInstanceWriter;
import io.camunda.operate.webapp.zeebe.operation.AbstractOperationHandler;
import io.camunda.operate.webapp.zeebe.operation.OperationHandler;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteProcessInstanceHandler
extends AbstractOperationHandler
implements OperationHandler {
    @Autowired
    private ProcessInstanceReader processInstanceReader;
    @Autowired
    private ProcessInstanceWriter processInstanceWriter;
    @Autowired
    private OperationsManager operationsManager;

    public void handleWithException(OperationEntity operation) throws Exception {
        if (operation.getProcessInstanceKey() == null) {
            this.failOperation(operation, "No process instance id is provided.");
            return;
        }
        this.markAsSent(operation);
        ProcessInstanceForListViewEntity processInstance = this.processInstanceReader.getProcessInstanceByKey(operation.getProcessInstanceKey());
        Long processInstanceKey = processInstance.getProcessInstanceKey();
        this.processInstanceWriter.deleteInstanceById(processInstanceKey);
        this.completeOperation(operation);
    }

    private void completeOperation(OperationEntity operation) throws PersistenceException {
        this.operationsManager.completeOperation(operation);
    }

    public Set<OperationType> getTypes() {
        return Set.of(OperationType.DELETE_PROCESS_INSTANCE);
    }
}
