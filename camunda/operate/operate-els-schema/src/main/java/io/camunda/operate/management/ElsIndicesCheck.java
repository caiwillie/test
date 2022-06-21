package io.camunda.operate.management;

import io.camunda.operate.schema.IndexSchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ElsIndicesCheck {
   @Autowired
   private IndexSchemaValidator indexSchemaValidator;

   public boolean indicesArePresent() {
      return this.indexSchemaValidator.schemaExists();
   }
}
