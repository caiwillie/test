package io.camunda.operate.schema;

public interface Versionable {
   String DEFAULT_SCHEMA_VERSION = "1.0.0";

   default String getVersion() {
      return "1.0.0";
   }
}
