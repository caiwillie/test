package io.camunda.operate.property;

public class AlertingProperties {
   private String webhook;

   public String getWebhook() {
      return this.webhook;
   }

   public AlertingProperties setWebhook(String webhook) {
      this.webhook = webhook;
      return this;
   }
}
