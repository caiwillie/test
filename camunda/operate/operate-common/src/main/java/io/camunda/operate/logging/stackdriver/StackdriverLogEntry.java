package io.camunda.operate.logging.stackdriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public final class StackdriverLogEntry {
   public static final String ERROR_REPORT_TYPE = "type.googleapis.com/google.devtools.clouderrorreporting.v1beta1.ReportedErrorEvent";
   @JsonProperty("severity")
   private String severity;
   @JsonProperty("logging.googleapis.com/sourceLocation")
   private SourceLocation sourceLocation;
   @JsonProperty(
      value = "message",
      required = true
   )
   private String message;
   @JsonProperty("serviceContext")
   private ServiceContext service;
   @JsonProperty("context")
   private Map context;
   @JsonProperty("@type")
   private String type;
   @JsonProperty("exception")
   private String exception;
   @JsonProperty("timestampSeconds")
   private Long timestampSeconds;
   @JsonProperty("timestampNanos")
   private Long timestampNanos;

   StackdriverLogEntry() {
   }

   public static StackdriverLogEntryBuilder builder() {
      return new StackdriverLogEntryBuilder();
   }

   public String getSeverity() {
      return this.severity;
   }

   public void setSeverity(String severity) {
      this.severity = severity;
   }

   public SourceLocation getSourceLocation() {
      return this.sourceLocation;
   }

   public void setSourceLocation(SourceLocation sourceLocation) {
      this.sourceLocation = sourceLocation;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public ServiceContext getService() {
      return this.service;
   }

   public void setService(ServiceContext service) {
      this.service = service;
   }

   public Map getContext() {
      return this.context;
   }

   public void setContext(Map context) {
      this.context = context;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getException() {
      return this.exception;
   }

   public void setException(String exception) {
      this.exception = exception;
   }

   public long getTimestampSeconds() {
      return this.timestampSeconds;
   }

   public void setTimestampSeconds(long timestampSeconds) {
      this.timestampSeconds = timestampSeconds;
   }

   public long getTimestampNanos() {
      return this.timestampNanos;
   }

   public void setTimestampNanos(long timestampNanos) {
      this.timestampNanos = timestampNanos;
   }
}
