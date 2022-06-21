package io.camunda.operate.webapp.security.sso.model;

import java.io.Serializable;

public class ClusterInfo implements Serializable {
   private String name;
   private OrgPermissions permissions;

   public ClusterInfo() {
   }

   public ClusterInfo(String name, OrgPermissions permissions) {
      this.name = name;
      this.permissions = permissions;
   }

   public String getName() {
      return this.name;
   }

   public OrgPermissions getPermissions() {
      return this.permissions;
   }

   public static class Permission {
      private Boolean read;
      private Boolean create;
      private Boolean update;
      private Boolean delete;

      public Permission() {
      }

      public Permission(Boolean read, Boolean create, Boolean update, Boolean delete) {
         this.read = read;
         this.create = create;
         this.update = update;
         this.delete = delete;
      }

      public Boolean getRead() {
         return this.read;
      }

      public Boolean getCreate() {
         return this.create;
      }

      public Boolean getUpdate() {
         return this.update;
      }

      public Boolean getDelete() {
         return this.delete;
      }
   }

   public static class OrgPermissions {
      private OrgPermissions cluster;
      private Permission operate;

      public OrgPermissions() {
      }

      public OrgPermissions(OrgPermissions permissions, Permission operate) {
         this.cluster = permissions;
         this.operate = operate;
      }

      public Permission getOperate() {
         return this.operate;
      }

      public OrgPermissions getCluster() {
         return this.cluster;
      }
   }
}
