package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.SetVariablesResponse;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateVariableHandler extends AbstractOperationHandler implements OperationHandler {
   @Autowired
   private ZeebeClient zeebeClient;

   public void handleWithException(OperationEntity operation) throws Exception {
      String updateVariableJson = this.mergeVariableJson(operation.getVariableName(), operation.getVariableValue());
      SetVariablesResponse response = (SetVariablesResponse)this.zeebeClient.newSetVariablesCommand(operation.getScopeKey()).variables(updateVariableJson).local(true).send().join();
      this.markAsSent(operation, response.getKey());
   }

   private String mergeVariableJson(String variableName, String variableValue) {
      return String.format("{\"%s\":%s}", variableName, variableValue);
   }

   public Set getTypes() {
      return Set.of(OperationType.UPDATE_VARIABLE, OperationType.ADD_VARIABLE);
   }

   public void setZeebeClient(ZeebeClient zeebeClient) {
      this.zeebeClient = zeebeClient;
   }
}
