package io.camunda.operate.property;

public class SslProperties {
   private String certificatePath;
   private boolean verifyHostname = true;
   private boolean selfSigned = false;

   public String getCertificatePath() {
      return this.certificatePath;
   }

   public void setCertificatePath(String certificatePath) {
      this.certificatePath = certificatePath;
   }

   public boolean isVerifyHostname() {
      return this.verifyHostname;
   }

   public void setVerifyHostname(boolean verifyHostname) {
      this.verifyHostname = verifyHostname;
   }

   public boolean isSelfSigned() {
      return this.selfSigned;
   }

   public void setSelfSigned(boolean selfSigned) {
      this.selfSigned = selfSigned;
   }
}
