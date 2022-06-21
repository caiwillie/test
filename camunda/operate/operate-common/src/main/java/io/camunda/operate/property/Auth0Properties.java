package io.camunda.operate.property;

public class Auth0Properties {
   private String domain;
   private String backendDomain;
   private String clientId;
   private String clientSecret;
   private String claimName;
   private String nameKey = "name";
   private String m2mClientId;
   private String m2mClientSecret;
   private String m2mAudience;

   public String getDomain() {
      return this.domain;
   }

   public Auth0Properties setDomain(String domain) {
      this.domain = domain;
      return this;
   }

   public String getBackendDomain() {
      return this.backendDomain;
   }

   public Auth0Properties setBackendDomain(String backendDomain) {
      this.backendDomain = backendDomain;
      return this;
   }

   public String getClientId() {
      return this.clientId;
   }

   public Auth0Properties setClientId(String clientId) {
      this.clientId = clientId;
      return this;
   }

   public String getClientSecret() {
      return this.clientSecret;
   }

   public Auth0Properties setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
      return this;
   }

   public String getClaimName() {
      return this.claimName;
   }

   public Auth0Properties setClaimName(String claimName) {
      this.claimName = claimName;
      return this;
   }

   public String getNameKey() {
      return this.nameKey;
   }

   public Auth0Properties setNameKey(String nameKey) {
      this.nameKey = nameKey;
      return this;
   }

   public String getM2mClientId() {
      return this.m2mClientId;
   }

   public Auth0Properties setM2mClientId(String m2mClientId) {
      this.m2mClientId = m2mClientId;
      return this;
   }

   public String getM2mClientSecret() {
      return this.m2mClientSecret;
   }

   public Auth0Properties setM2mClientSecret(String m2mClientSecret) {
      this.m2mClientSecret = m2mClientSecret;
      return this;
   }

   public String getM2mAudience() {
      return this.m2mAudience;
   }

   public Auth0Properties setM2mAudience(String m2mAudience) {
      this.m2mAudience = m2mAudience;
      return this;
   }
}
