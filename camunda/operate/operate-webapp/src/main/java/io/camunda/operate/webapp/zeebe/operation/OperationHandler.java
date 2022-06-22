/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationType
 */
package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import java.util.Set;

public interface OperationHandler {
    public void handle(OperationEntity var1);

    public void handleWithException(OperationEntity var1) throws Exception;

    public Set<OperationType> getTypes();
}
