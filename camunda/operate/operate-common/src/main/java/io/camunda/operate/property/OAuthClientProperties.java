package io.camunda.operate.property;

public class OAuthClientProperties {
   public static final String DEFAULT_AUDIENCE = "operate.camunda.io";
   private String audience = "operate.camunda.io";
   private String clusterId;
   private String scope;

   public String getClusterId() {
      return this.clusterId;
   }

   public void setClusterId(String clusterId) {
      this.clusterId = clusterId;
   }

   public String getAudience() {
      return this.audience;
   }

   public void setAudience(String audience) {
      this.audience = audience;
   }

   public String getScope() {
      return this.scope != null && !this.scope.isEmpty() ? this.scope : this.clusterId;
   }

   public void setScope(String scope) {
      this.scope = scope;
   }
}
