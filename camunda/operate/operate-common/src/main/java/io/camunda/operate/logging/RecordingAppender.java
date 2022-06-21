package io.camunda.operate.logging;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.NullAppender;

public final class RecordingAppender implements Appender {
   private final Appender delegate;
   private final List appendedEvents;

   public RecordingAppender(Appender delegate) {
      this.delegate = delegate;
      this.appendedEvents = new ArrayList();
   }

   public RecordingAppender() {
      this(NullAppender.createAppender("RecordingAppender"));
   }

   public void append(LogEvent event) {
      this.appendedEvents.add(event.toImmutable());
      this.delegate.append(event);
   }

   public String getName() {
      return this.delegate.getName();
   }

   public Layout getLayout() {
      return this.delegate.getLayout();
   }

   public boolean ignoreExceptions() {
      return this.delegate.ignoreExceptions();
   }

   public ErrorHandler getHandler() {
      return this.delegate.getHandler();
   }

   public void setHandler(ErrorHandler handler) {
      this.delegate.setHandler(handler);
   }

   public List getAppendedEvents() {
      return this.appendedEvents;
   }

   public LifeCycle.State getState() {
      return this.delegate.getState();
   }

   public void initialize() {
      this.delegate.initialize();
   }

   public void start() {
      this.delegate.start();
   }

   public void stop() {
      this.delegate.stop();
   }

   public boolean isStarted() {
      return this.delegate.isStarted();
   }

   public boolean isStopped() {
      return this.delegate.isStopped();
   }
}
