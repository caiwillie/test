package io.camunda.operate.entities;

import java.time.OffsetDateTime;
import java.util.Objects;

public class MetricEntity extends OperateEntity {
   private String event;
   private String value;
   private OffsetDateTime eventTime;

   public MetricEntity() {
   }

   public MetricEntity(String event, String value, OffsetDateTime eventTime) {
      this.event = event;
      this.value = value;
      this.eventTime = eventTime;
   }

   public String getEvent() {
      return this.event;
   }

   public MetricEntity setEvent(String event) {
      this.event = event;
      return this;
   }

   public String getValue() {
      return this.value;
   }

   public MetricEntity setValue(String value) {
      this.value = value;
      return this;
   }

   public OffsetDateTime getEventTime() {
      return this.eventTime;
   }

   public MetricEntity setEventTime(OffsetDateTime eventTime) {
      this.eventTime = eventTime;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof MetricEntity)) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         MetricEntity that = (MetricEntity)o;
         return Objects.equals(this.event, that.event) && Objects.equals(this.value, that.value) && Objects.equals(this.eventTime, that.eventTime);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.event, this.value, this.eventTime});
   }
}
