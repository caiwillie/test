/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.event.ApplicationFailedEvent
 *  org.springframework.context.ApplicationListener
 */
package io.camunda.operate;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

/*
public static class ApplicationErrorListener implements ApplicationListener<ApplicationFailedEvent> {
    public void onApplicationEvent(ApplicationFailedEvent event) {
        event.getApplicationContext().close();
        System.exit(-1);
    }
}
*/
