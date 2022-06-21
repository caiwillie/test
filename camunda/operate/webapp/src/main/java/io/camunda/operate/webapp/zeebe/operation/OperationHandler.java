package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import java.util.Set;

public interface OperationHandler {
   void handle(OperationEntity var1);

   void handleWithException(OperationEntity var1) throws Exception;

   Set getTypes();
}
