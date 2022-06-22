/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.webapp.zeebe.operation.OperationHandler
 */
package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.webapp.zeebe.operation.OperationHandler;

public class OperationCommand
implements Runnable {
    private OperationEntity entity;
    private OperationHandler handler;

    public OperationCommand(OperationEntity entity, OperationHandler handler) {
        this.entity = entity;
        this.handler = handler;
    }

    @Override
    public void run() {
        this.handler.handle(this.entity);
    }
}
