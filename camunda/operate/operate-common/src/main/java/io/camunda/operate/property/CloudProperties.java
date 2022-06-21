package io.camunda.operate.property;

public class CloudProperties {
   private String organizationId;
   private String clusterId;
   private String mixpanelToken;
   private String mixpanelAPIHost;
   private String permissionUrl;
   private String permissionAudience;

   public String getPermissionUrl() {
      return this.permissionUrl;
   }

   public void setPermissionUrl(String permissionUrl) {
      this.permissionUrl = permissionUrl;
   }

   public String getPermissionAudience() {
      return this.permissionAudience;
   }

   public void setPermissionAudience(String permissionAudience) {
      this.permissionAudience = permissionAudience;
   }

   public String getOrganizationId() {
      return this.organizationId;
   }

   public CloudProperties setOrganizationId(String organizationId) {
      this.organizationId = organizationId;
      return this;
   }

   public String getClusterId() {
      return this.clusterId;
   }

   public CloudProperties setClusterId(String clusterId) {
      this.clusterId = clusterId;
      return this;
   }

   public String getMixpanelToken() {
      return this.mixpanelToken;
   }

   public CloudProperties setMixpanelToken(String mixpanelToken) {
      this.mixpanelToken = mixpanelToken;
      return this;
   }

   public String getMixpanelAPIHost() {
      return this.mixpanelAPIHost;
   }

   public CloudProperties setMixpanelAPIHost(String mixpanelAPIHost) {
      this.mixpanelAPIHost = mixpanelAPIHost;
      return this;
   }
}
