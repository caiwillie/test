package io.camunda.operate.schema.indices;

import io.camunda.operate.schema.Versionable;

public interface IndexDescriptor extends Versionable {
   String getIndexName();

   String getFullQualifiedName();

   default String getDerivedIndexNamePattern() {
      return this.getFullQualifiedName() + "*";
   }

   default String getAlias() {
      return this.getFullQualifiedName() + "alias";
   }
}
