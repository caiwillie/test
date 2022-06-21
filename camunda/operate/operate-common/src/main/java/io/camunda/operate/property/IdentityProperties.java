package io.camunda.operate.property;

public class IdentityProperties {
   private String issuerUrl;
   private String issuerBackendUrl;
   private String clientId;
   private String clientSecret;
   private String audience;

   public String getIssuerUrl() {
      return this.issuerUrl;
   }

   public void setIssuerUrl(String issuerUrl) {
      this.issuerUrl = issuerUrl;
   }

   public String getClientId() {
      return this.clientId;
   }

   public void setClientId(String clientId) {
      this.clientId = clientId;
   }

   public String getClientSecret() {
      return this.clientSecret;
   }

   public void setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
   }

   public String getIssuerBackendUrl() {
      return this.issuerBackendUrl;
   }

   public void setIssuerBackendUrl(String issuerBackendUrl) {
      this.issuerBackendUrl = issuerBackendUrl;
   }

   public String getAudience() {
      return this.audience;
   }

   public void setAudience(String audience) {
      this.audience = audience;
   }
}
