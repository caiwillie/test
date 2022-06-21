package io.camunda.operate.entities;

import io.camunda.operate.util.ConversionUtils;

public class ProcessEntity extends OperateZeebeEntity {
   private String name;
   private int version;
   private String bpmnProcessId;
   private String bpmnXml;
   private String resourceName;

   public String getName() {
      return this.name;
   }

   public ProcessEntity setId(String id) {
      super.setId(id);
      this.setKey(ConversionUtils.toLongOrNull(id));
      return this;
   }

   public ProcessEntity setName(String name) {
      this.name = name;
      return this;
   }

   public int getVersion() {
      return this.version;
   }

   public ProcessEntity setVersion(int version) {
      this.version = version;
      return this;
   }

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public ProcessEntity setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
      return this;
   }

   public String getBpmnXml() {
      return this.bpmnXml;
   }

   public ProcessEntity setBpmnXml(String bpmnXml) {
      this.bpmnXml = bpmnXml;
      return this;
   }

   public String getResourceName() {
      return this.resourceName;
   }

   public ProcessEntity setResourceName(String resourceName) {
      this.resourceName = resourceName;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            ProcessEntity that = (ProcessEntity)o;
            if (this.version != that.version) {
               return false;
            } else {
               label58: {
                  if (this.name != null) {
                     if (this.name.equals(that.name)) {
                        break label58;
                     }
                  } else if (that.name == null) {
                     break label58;
                  }

                  return false;
               }

               if (this.bpmnProcessId != null) {
                  if (!this.bpmnProcessId.equals(that.bpmnProcessId)) {
                     return false;
                  }
               } else if (that.bpmnProcessId != null) {
                  return false;
               }

               if (this.bpmnXml != null) {
                  if (this.bpmnXml.equals(that.bpmnXml)) {
                     return this.resourceName != null ? this.resourceName.equals(that.resourceName) : that.resourceName == null;
                  }
               } else if (that.bpmnXml == null) {
                  return this.resourceName != null ? this.resourceName.equals(that.resourceName) : that.resourceName == null;
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
      result = 31 * result + this.version;
      result = 31 * result + (this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0);
      result = 31 * result + (this.bpmnXml != null ? this.bpmnXml.hashCode() : 0);
      result = 31 * result + (this.resourceName != null ? this.resourceName.hashCode() : 0);
      return result;
   }

   public String toString() {
      String var10000 = this.name;
      return "ProcessEntity{name='" + var10000 + "', version=" + this.version + ", bpmnProcessId='" + this.bpmnProcessId + "', bpmnXml='" + this.bpmnXml + "', resourceName='" + this.resourceName + "'} " + super.toString();
   }
}
