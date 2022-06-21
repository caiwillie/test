package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;

public class OperationCommand implements Runnable {
   private OperationEntity entity;
   private OperationHandler handler;

   public OperationCommand(OperationEntity entity, OperationHandler handler) {
      this.entity = entity;
      this.handler = handler;
   }

   public void run() {
      this.handler.handle(this.entity);
   }
}
