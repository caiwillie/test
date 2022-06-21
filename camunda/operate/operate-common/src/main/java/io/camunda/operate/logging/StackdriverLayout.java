package io.camunda.operate.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.camunda.operate.logging.stackdriver.StackdriverLogEntry;
import io.camunda.operate.logging.stackdriver.StackdriverLogEntryBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;

@Plugin(
   name = "StackdriverLayout",
   category = "Core",
   elementType = "layout"
)
public final class StackdriverLayout extends AbstractLayout implements LocationAware {
   private static final ObjectWriter WRITER = (new ObjectMapper()).writerFor(StackdriverLogEntry.class);
   private static final String CONTENT_TYPE = "application/json; charset=utf-8";
   private static final String DEFAULT_SERVICE_VERSION = "development";
   private static final String DEFAULT_SERVICE_NAME = "zeebe";
   private static final byte[] EMPTY = new byte[0];
   private static final byte[] LINE_SEPARATOR;
   private final String serviceName;
   private final String serviceVersion;

   public StackdriverLayout() {
      this(new DefaultConfiguration(), "zeebe", "development");
   }

   public StackdriverLayout(Configuration configuration, String serviceName, String serviceVersion) {
      super(configuration, (byte[])null, (byte[])null);
      if (serviceName != null && !serviceName.isBlank()) {
         this.serviceName = serviceName;
      } else {
         this.serviceName = "zeebe";
      }

      if (serviceVersion != null && !serviceVersion.isBlank()) {
         this.serviceVersion = serviceVersion;
      } else {
         this.serviceVersion = "development";
      }

   }

   @PluginBuilderFactory
   public static Builder newBuilder() {
      return (Builder)(new Builder()).asBuilder();
   }

   public byte[] toByteArray(LogEvent event) {
      return this.toSerializable(event);
   }

   public byte[] toSerializable(LogEvent event) {
      StackdriverLogEntry entry = this.buildLogEntry(event);

      try {
         ByteArrayOutputStream output = new ByteArrayOutputStream();

         byte[] var4;
         try {
            WRITER.writeValue(output, entry);
            output.write(LINE_SEPARATOR);
            var4 = output.toByteArray();
         } catch (Throwable var7) {
            try {
               output.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         output.close();
         return var4;
      } catch (IOException var8) {
         LOGGER.error(var8);
         return EMPTY;
      }
   }

   public String getContentType() {
      return "application/json; charset=utf-8";
   }

   public void encode(LogEvent event, ByteBufferDestination destination) {
      StackdriverLogEntry entry = this.buildLogEntry(event);

      try {
         ByteBufferDestinationOutputStream output = new ByteBufferDestinationOutputStream(destination);

         try {
            WRITER.writeValue(output, entry);
            output.write(LINE_SEPARATOR);
         } catch (Throwable var8) {
            try {
               output.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         output.close();
      } catch (IOException var9) {
         LOGGER.error(var9);
      }

   }

   public boolean requiresLocation() {
      return false;
   }

   private StackdriverLogEntry buildLogEntry(LogEvent event) {
      StackdriverLogEntryBuilder builder = StackdriverLogEntry.builder().withLevel(event.getLevel()).withMessage(event.getMessage().getFormattedMessage()).withTime(event.getInstant()).withDiagnosticContext(event.getContextData()).withThreadId(event.getThreadId()).withThreadPriority(event.getThreadPriority()).withServiceName(this.serviceName).withServiceVersion(this.serviceVersion);
      StackTraceElement source = event.getSource();
      if (source != null) {
         builder.withSource(source);
      }

      ThrowableProxy thrownProxy = event.getThrownProxy();
      if (thrownProxy != null) {
         builder.withException(thrownProxy);
      }

      String threadName = event.getThreadName();
      if (threadName != null) {
         builder.withThreadName(threadName);
      }

      String loggerName = event.getLoggerName();
      if (loggerName != null) {
         builder.withLogger(loggerName);
      }

      return builder.build();
   }

   static {
      LINE_SEPARATOR = System.lineSeparator().getBytes(StandardCharsets.UTF_8);
   }

   public static class Builder extends AbstractLayout.Builder implements org.apache.logging.log4j.core.util.Builder {
      @PluginBuilderAttribute("serviceName")
      private String serviceName;
      @PluginBuilderAttribute("serviceVersion")
      private String serviceVersion;

      public StackdriverLayout build() {
         return new StackdriverLayout(this.getConfiguration(), this.getServiceName(), this.getServiceVersion());
      }

      public String getServiceName() {
         return this.serviceName;
      }

      public Builder setServiceName(String serviceName) {
         this.serviceName = serviceName;
         return (Builder)this.asBuilder();
      }

      public String getServiceVersion() {
         return this.serviceVersion;
      }

      public Builder setServiceVersion(String serviceVersion) {
         this.serviceVersion = serviceVersion;
         return (Builder)this.asBuilder();
      }
   }
}
