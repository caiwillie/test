package io.camunda.operate.property;

public class ZeebeElasticsearchProperties extends ElasticsearchProperties {
   private String prefix = "zeebe-record";

   public ZeebeElasticsearchProperties() {
      this.setDateFormat("yyyy-MM-dd");
      this.setElsDateFormat("date");
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }
}
