package io.camunda.operate.property;

public class LdapProperties {
   private String url;
   private String baseDn;
   private String userDnPatterns;
   private String userSearchBase;
   private String managerDn;
   private String managerPassword;
   private String userSearchFilter;
   private String domain;
   private String firstnameAttrName = "givenName";
   private String lastnameAttrName = "sn";
   private String displayNameAttrName = "displayname";
   private String userIdAttrName = "uid";

   public String getBaseDn() {
      return this.baseDn;
   }

   public void setBaseDn(String baseDn) {
      this.baseDn = baseDn;
   }

   public String getUserSearchBase() {
      return this.userSearchBase == null ? "" : this.userSearchBase;
   }

   public void setUserSearchBase(String userSearchBase) {
      this.userSearchBase = userSearchBase;
   }

   public String getManagerDn() {
      return this.managerDn;
   }

   public void setManagerDn(String managerDn) {
      this.managerDn = managerDn;
   }

   public String getManagerPassword() {
      return this.managerPassword;
   }

   public void setManagerPassword(String managerPassword) {
      this.managerPassword = managerPassword;
   }

   public String getUserSearchFilter() {
      return this.userSearchFilter;
   }

   public void setUserSearchFilter(String userSearchFilter) {
      this.userSearchFilter = userSearchFilter;
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getUserDnPatterns() {
      return this.userDnPatterns == null ? "" : this.userDnPatterns;
   }

   public void setUserDnPatterns(String userDnPatterns) {
      this.userDnPatterns = userDnPatterns;
   }

   public String getDomain() {
      return this.domain;
   }

   public void setDomain(String domain) {
      this.domain = domain;
   }

   public String getFirstnameAttrName() {
      return this.firstnameAttrName;
   }

   public void setFirstnameAttrName(String firstnameAttrName) {
      this.firstnameAttrName = firstnameAttrName;
   }

   public String getLastnameAttrName() {
      return this.lastnameAttrName;
   }

   public void setLastnameAttrName(String lastnameAttrName) {
      this.lastnameAttrName = lastnameAttrName;
   }

   public String getDisplayNameAttrName() {
      return this.displayNameAttrName;
   }

   public void setDisplayNameAttrName(String displayNameAttrName) {
      this.displayNameAttrName = displayNameAttrName;
   }

   public String getUserIdAttrName() {
      return this.userIdAttrName;
   }

   public void setUserIdAttrName(String userIdAttrName) {
      this.userIdAttrName = userIdAttrName;
   }
}
