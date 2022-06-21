package io.camunda.operate.logging.stackdriver;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

public final class StackdriverLogEntryBuilder {
   public static final String ERROR_REPORT_LOCATION_CONTEXT_KEY = "reportLocation";
   private final ServiceContext service = new ServiceContext();
   private final Map context = new HashMap();
   private SourceLocation sourceLocation;
   private Severity severity;
   private String message;
   private StackTraceElement traceElement;
   private String type;
   private String exception;
   private Instant time;

   StackdriverLogEntryBuilder() {
   }

   public StackdriverLogEntryBuilder withLevel(Level level) {
      switch (level.getStandardLevel()) {
         case FATAL:
            this.severity = Severity.CRITICAL;
            break;
         case ERROR:
            this.severity = Severity.ERROR;
            break;
         case WARN:
            this.severity = Severity.WARNING;
            break;
         case INFO:
            this.severity = Severity.INFO;
            break;
         case DEBUG:
         case TRACE:
            this.severity = Severity.DEBUG;
            break;
         case OFF:
         case ALL:
         default:
            this.severity = Severity.DEFAULT;
      }

      return this;
   }

   public StackdriverLogEntryBuilder withSource(StackTraceElement traceElement) {
      this.traceElement = traceElement;
      return this;
   }

   public StackdriverLogEntryBuilder withTime(Instant time) {
      this.time = time;
      return this;
   }

   public StackdriverLogEntryBuilder withMessage(String message) {
      this.message = message;
      return this;
   }

   public StackdriverLogEntryBuilder withServiceName(String serviceName) {
      this.service.setService(serviceName);
      return this;
   }

   public StackdriverLogEntryBuilder withServiceVersion(String serviceVersion) {
      this.service.setVersion(serviceVersion);
      return this;
   }

   public StackdriverLogEntryBuilder withContextEntry(String key, Object value) {
      this.context.put(key, value);
      return this;
   }

   public StackdriverLogEntryBuilder withDiagnosticContext(ReadOnlyStringMap context) {
      context.forEach(this::withContextEntry);
      return this;
   }

   public StackdriverLogEntryBuilder withException(ThrowableProxy error) {
      return this.withException(error.getExtendedStackTraceAsString());
   }

   public StackdriverLogEntryBuilder withType(String type) {
      this.type = type;
      return this;
   }

   public StackdriverLogEntryBuilder withException(String exception) {
      this.exception = exception;
      return this;
   }

   public StackdriverLogEntryBuilder withLogger(String logger) {
      return this.withContextEntry("loggerName", logger);
   }

   public StackdriverLogEntryBuilder withThreadName(String threadName) {
      return this.withContextEntry("threadName", threadName);
   }

   public StackdriverLogEntryBuilder withThreadId(long threadId) {
      return this.withContextEntry("threadId", threadId);
   }

   public StackdriverLogEntryBuilder withThreadPriority(int threadPriority) {
      return this.withContextEntry("threadPriority", threadPriority);
   }

   public StackdriverLogEntry build() {
      StackdriverLogEntry stackdriverLogEntry = new StackdriverLogEntry();
      if (this.traceElement != null) {
         this.sourceLocation = this.mapStackTraceToSourceLocation(this.traceElement);
         if (this.severity == Severity.ERROR && this.exception == null) {
            this.context.putIfAbsent("reportLocation", this.mapStackTraceToReportLocation(this.traceElement));
         }
      }

      if (this.severity == Severity.ERROR && this.type == null) {
         this.type = "type.googleapis.com/google.devtools.clouderrorreporting.v1beta1.ReportedErrorEvent";
      }

      if (this.time != null) {
         stackdriverLogEntry.setTimestampSeconds(this.time.getEpochSecond());
         stackdriverLogEntry.setTimestampNanos((long)this.time.getNanoOfSecond());
      }

      stackdriverLogEntry.setSeverity(this.severity.name());
      stackdriverLogEntry.setSourceLocation(this.sourceLocation);
      stackdriverLogEntry.setMessage((String)Objects.requireNonNull(this.message));
      stackdriverLogEntry.setService(this.service);
      stackdriverLogEntry.setContext(this.context);
      stackdriverLogEntry.setType(this.type);
      stackdriverLogEntry.setException(this.exception);
      return stackdriverLogEntry;
   }

   private SourceLocation mapStackTraceToSourceLocation(StackTraceElement stackTrace) {
      SourceLocation location = new SourceLocation();
      location.setFile(stackTrace.getFileName());
      location.setFunction(stackTrace.getMethodName());
      location.setLine(stackTrace.getLineNumber());
      return location;
   }

   private ReportLocation mapStackTraceToReportLocation(StackTraceElement stackTrace) {
      ReportLocation location = new ReportLocation();
      location.setFilePath(stackTrace.getFileName());
      location.setFunctionName(stackTrace.getMethodName());
      location.setLineNumber(stackTrace.getLineNumber());
      return location;
   }
}
