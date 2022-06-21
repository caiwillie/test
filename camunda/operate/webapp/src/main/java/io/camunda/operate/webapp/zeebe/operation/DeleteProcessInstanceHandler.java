package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.util.OperationsManager;
import io.camunda.operate.webapp.es.reader.ProcessInstanceReader;
import io.camunda.operate.webapp.es.writer.ProcessInstanceWriter;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteProcessInstanceHandler extends AbstractOperationHandler implements OperationHandler {
   @Autowired
   private ProcessInstanceReader processInstanceReader;
   @Autowired
   private ProcessInstanceWriter processInstanceWriter;
   @Autowired
   private OperationsManager operationsManager;

   public void handleWithException(OperationEntity operation) throws Exception {
      if (operation.getProcessInstanceKey() == null) {
         this.failOperation(operation, "No process instance id is provided.");
      } else {
         this.markAsSent(operation);
         ProcessInstanceForListViewEntity processInstance = this.processInstanceReader.getProcessInstanceByKey(operation.getProcessInstanceKey());
         Long processInstanceKey = processInstance.getProcessInstanceKey();
         this.processInstanceWriter.deleteInstanceById(processInstanceKey);
         this.completeOperation(operation);
      }
   }

   private void completeOperation(OperationEntity operation) throws PersistenceException {
      this.operationsManager.completeOperation(operation);
   }

   public Set getTypes() {
      return Set.of(OperationType.DELETE_PROCESS_INSTANCE);
   }
}
