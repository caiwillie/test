package io.camunda.operate.schema.templates;

import io.camunda.operate.property.OperateProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractTemplateDescriptor implements TemplateDescriptor {
   @Autowired
   private OperateProperties operateProperties;

   public String getFullQualifiedName() {
      return String.format("%s-%s-%s_", this.operateProperties.getElasticsearch().getIndexPrefix(), this.getIndexName(), this.getVersion());
   }
}
