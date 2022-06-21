package io.camunda.operate.property;

public class ZeebeProperties {
   private String gatewayAddress = "localhost:26500";
   private boolean isSecure = false;
   private String certificatePath = null;

   public boolean isSecure() {
      return this.isSecure;
   }

   public ZeebeProperties setSecure(boolean secure) {
      this.isSecure = secure;
      return this;
   }

   public String getCertificatePath() {
      return this.certificatePath;
   }

   public ZeebeProperties setCertificatePath(String caCertificatePath) {
      this.certificatePath = caCertificatePath;
      return this;
   }

   /** @deprecated */
   @Deprecated
   public String getBrokerContactPoint() {
      return this.gatewayAddress;
   }

   /** @deprecated */
   @Deprecated
   public void setBrokerContactPoint(String brokerContactPoint) {
      this.gatewayAddress = brokerContactPoint;
   }

   public String getGatewayAddress() {
      return this.gatewayAddress;
   }

   public ZeebeProperties setGatewayAddress(String gatewayAddress) {
      this.gatewayAddress = gatewayAddress;
      return this;
   }
}
