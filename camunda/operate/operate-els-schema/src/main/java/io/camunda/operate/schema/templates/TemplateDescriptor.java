package io.camunda.operate.schema.templates;

import io.camunda.operate.schema.indices.IndexDescriptor;

public interface TemplateDescriptor extends IndexDescriptor {
   String PARTITION_ID = "partitionId";

   default String getTemplateName() {
      return this.getFullQualifiedName() + "template";
   }

   default String getIndexPattern() {
      return this.getFullQualifiedName() + "*";
   }
}
