package io.camunda.operate.webapp.api.v1.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class ProcessDefinition {
   public static final String KEY = "key";
   public static final String NAME = "name";
   public static final String VERSION = "version";
   public static final String BPMN_PROCESS_ID = "bpmnProcessId";
   private Long key;
   private String name;
   private Integer version;
   private String bpmnProcessId;

   public Long getKey() {
      return this.key;
   }

   public ProcessDefinition setKey(long key) {
      this.key = key;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public ProcessDefinition setName(String name) {
      this.name = name;
      return this;
   }

   public Integer getVersion() {
      return this.version;
   }

   public ProcessDefinition setVersion(int version) {
      this.version = version;
      return this;
   }

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public ProcessDefinition setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ProcessDefinition that = (ProcessDefinition)o;
         return Objects.equals(this.key, that.key) && Objects.equals(this.name, that.name) && Objects.equals(this.version, that.version) && Objects.equals(this.bpmnProcessId, that.bpmnProcessId);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.key, this.name, this.version, this.bpmnProcessId});
   }

   public String toString() {
      return "ProcessDefinition{key=" + this.key + ", name='" + this.name + "', version=" + this.version + ", bpmnProcessId='" + this.bpmnProcessId + "'}";
   }
}
