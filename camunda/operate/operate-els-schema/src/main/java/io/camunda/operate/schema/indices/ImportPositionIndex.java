package io.camunda.operate.schema.indices;

import org.springframework.stereotype.Component;

@Component
public class ImportPositionIndex extends AbstractIndexDescriptor {
   public static final String INDEX_NAME = "import-position";
   public static final String ALIAS_NAME = "aliasName";
   public static final String ID = "id";
   public static final String POSITION = "position";
   public static final String FIELD_INDEX_NAME = "indexName";

   public String getIndexName() {
      return "import-position";
   }
}
