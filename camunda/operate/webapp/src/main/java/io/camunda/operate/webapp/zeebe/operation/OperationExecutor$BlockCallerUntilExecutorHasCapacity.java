/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.zeebe.operation;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
/*

private class BlockCallerUntilExecutorHasCapacity implements RejectedExecutionHandler {
    private BlockCallerUntilExecutorHasCapacity() {
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        if (executor.isShutdown()) return;
        try {
            executor.getQueue().put(runnable);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
*/
