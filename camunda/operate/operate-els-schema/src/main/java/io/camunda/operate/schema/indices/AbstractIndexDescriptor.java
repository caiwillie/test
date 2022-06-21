package io.camunda.operate.schema.indices;

import io.camunda.operate.property.OperateProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractIndexDescriptor implements IndexDescriptor {
   public static final String PARTITION_ID = "partitionId";
   @Autowired
   protected OperateProperties operateProperties;

   public String getFullQualifiedName() {
      return String.format("%s-%s-%s_", this.operateProperties.getElasticsearch().getIndexPrefix(), this.getIndexName(), this.getVersion());
   }
}
