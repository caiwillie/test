package io.camunda.operate.entities;

import java.time.OffsetDateTime;

public class EventEntity extends OperateZeebeEntity {
   private Long processDefinitionKey;
   private Long processInstanceKey;
   private String bpmnProcessId;
   private String flowNodeId;
   private Long flowNodeInstanceKey;
   private EventSourceType eventSourceType;
   private EventType eventType;
   private OffsetDateTime dateTime;
   private EventMetadataEntity metadata;

   public Long getProcessDefinitionKey() {
      return this.processDefinitionKey;
   }

   public void setProcessDefinitionKey(Long processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
   }

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public void setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
   }

   public String getFlowNodeId() {
      return this.flowNodeId;
   }

   public void setFlowNodeId(String flowNodeId) {
      this.flowNodeId = flowNodeId;
   }

   public Long getFlowNodeInstanceKey() {
      return this.flowNodeInstanceKey;
   }

   public void setFlowNodeInstanceKey(Long flowNodeInstanceKey) {
      this.flowNodeInstanceKey = flowNodeInstanceKey;
   }

   public EventSourceType getEventSourceType() {
      return this.eventSourceType;
   }

   public void setEventSourceType(EventSourceType eventSourceType) {
      this.eventSourceType = eventSourceType;
   }

   public EventType getEventType() {
      return this.eventType;
   }

   public void setEventType(EventType eventType) {
      this.eventType = eventType;
   }

   public String getBpmnProcessId() {
      return this.bpmnProcessId;
   }

   public void setBpmnProcessId(String bpmnProcessId) {
      this.bpmnProcessId = bpmnProcessId;
   }

   public OffsetDateTime getDateTime() {
      return this.dateTime;
   }

   public void setDateTime(OffsetDateTime dateTime) {
      this.dateTime = dateTime;
   }

   public EventMetadataEntity getMetadata() {
      return this.metadata;
   }

   public void setMetadata(EventMetadataEntity metadata) {
      this.metadata = metadata;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            EventEntity that = (EventEntity)o;
            if (this.processDefinitionKey != null) {
               if (!this.processDefinitionKey.equals(that.processDefinitionKey)) {
                  return false;
               }
            } else if (that.processDefinitionKey != null) {
               return false;
            }

            if (this.processInstanceKey != null) {
               if (!this.processInstanceKey.equals(that.processInstanceKey)) {
                  return false;
               }
            } else if (that.processInstanceKey != null) {
               return false;
            }

            label86: {
               if (this.bpmnProcessId != null) {
                  if (this.bpmnProcessId.equals(that.bpmnProcessId)) {
                     break label86;
                  }
               } else if (that.bpmnProcessId == null) {
                  break label86;
               }

               return false;
            }

            label79: {
               if (this.flowNodeId != null) {
                  if (this.flowNodeId.equals(that.flowNodeId)) {
                     break label79;
                  }
               } else if (that.flowNodeId == null) {
                  break label79;
               }

               return false;
            }

            if (this.flowNodeInstanceKey != null) {
               if (!this.flowNodeInstanceKey.equals(that.flowNodeInstanceKey)) {
                  return false;
               }
            } else if (that.flowNodeInstanceKey != null) {
               return false;
            }

            if (this.eventSourceType != that.eventSourceType) {
               return false;
            } else if (this.eventType != that.eventType) {
               return false;
            } else {
               if (this.dateTime != null) {
                  if (this.dateTime.equals(that.dateTime)) {
                     return this.metadata != null ? this.metadata.equals(that.metadata) : that.metadata == null;
                  }
               } else if (that.dateTime == null) {
                  return this.metadata != null ? this.metadata.equals(that.metadata) : that.metadata == null;
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
      result = 31 * result + (this.processDefinitionKey != null ? this.processDefinitionKey.hashCode() : 0);
      result = 31 * result + (this.processInstanceKey != null ? this.processInstanceKey.hashCode() : 0);
      result = 31 * result + (this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0);
      result = 31 * result + (this.flowNodeId != null ? this.flowNodeId.hashCode() : 0);
      result = 31 * result + (this.flowNodeInstanceKey != null ? this.flowNodeInstanceKey.hashCode() : 0);
      result = 31 * result + (this.eventSourceType != null ? this.eventSourceType.hashCode() : 0);
      result = 31 * result + (this.eventType != null ? this.eventType.hashCode() : 0);
      result = 31 * result + (this.dateTime != null ? this.dateTime.hashCode() : 0);
      result = 31 * result + (this.metadata != null ? this.metadata.hashCode() : 0);
      return result;
   }
}
