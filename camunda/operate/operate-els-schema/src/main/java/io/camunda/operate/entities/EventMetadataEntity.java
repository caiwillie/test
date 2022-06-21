package io.camunda.operate.entities;

import java.time.OffsetDateTime;
import java.util.Map;

public class EventMetadataEntity {
   private String jobType;
   private Integer jobRetries;
   private String jobWorker;
   private OffsetDateTime jobDeadline;
   private Map jobCustomHeaders;
   private ErrorType incidentErrorType;
   private String incidentErrorMessage;
   private Long jobKey;

   public String getJobType() {
      return this.jobType;
   }

   public void setJobType(String jobType) {
      this.jobType = jobType;
   }

   public Integer getJobRetries() {
      return this.jobRetries;
   }

   public void setJobRetries(Integer jobRetries) {
      this.jobRetries = jobRetries;
   }

   public String getJobWorker() {
      return this.jobWorker;
   }

   public void setJobWorker(String jobWorker) {
      this.jobWorker = jobWorker;
   }

   public OffsetDateTime getJobDeadline() {
      return this.jobDeadline;
   }

   public void setJobDeadline(OffsetDateTime jobDeadline) {
      this.jobDeadline = jobDeadline;
   }

   public Map getJobCustomHeaders() {
      return this.jobCustomHeaders;
   }

   public void setJobCustomHeaders(Map jobCustomHeaders) {
      this.jobCustomHeaders = jobCustomHeaders;
   }

   public ErrorType getIncidentErrorType() {
      return this.incidentErrorType;
   }

   public void setIncidentErrorType(ErrorType incidentErrorType) {
      this.incidentErrorType = incidentErrorType;
   }

   public String getIncidentErrorMessage() {
      return this.incidentErrorMessage;
   }

   public void setIncidentErrorMessage(String incidentErrorMessage) {
      this.incidentErrorMessage = incidentErrorMessage;
   }

   public Long getJobKey() {
      return this.jobKey;
   }

   public void setJobKey(Long jobKey) {
      this.jobKey = jobKey;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EventMetadataEntity that = (EventMetadataEntity)o;
         if (this.jobType != null) {
            if (!this.jobType.equals(that.jobType)) {
               return false;
            }
         } else if (that.jobType != null) {
            return false;
         }

         label94: {
            if (this.jobRetries != null) {
               if (this.jobRetries.equals(that.jobRetries)) {
                  break label94;
               }
            } else if (that.jobRetries == null) {
               break label94;
            }

            return false;
         }

         label87: {
            if (this.jobWorker != null) {
               if (this.jobWorker.equals(that.jobWorker)) {
                  break label87;
               }
            } else if (that.jobWorker == null) {
               break label87;
            }

            return false;
         }

         if (this.jobDeadline != null) {
            if (!this.jobDeadline.equals(that.jobDeadline)) {
               return false;
            }
         } else if (that.jobDeadline != null) {
            return false;
         }

         label73: {
            if (this.jobCustomHeaders != null) {
               if (this.jobCustomHeaders.equals(that.jobCustomHeaders)) {
                  break label73;
               }
            } else if (that.jobCustomHeaders == null) {
               break label73;
            }

            return false;
         }

         if (this.incidentErrorType != null) {
            if (!this.incidentErrorType.equals(that.incidentErrorType)) {
               return false;
            }
         } else if (that.incidentErrorType != null) {
            return false;
         }

         if (this.incidentErrorMessage != null) {
            if (this.incidentErrorMessage.equals(that.incidentErrorMessage)) {
               return this.jobKey != null ? this.jobKey.equals(that.jobKey) : that.jobKey == null;
            }
         } else if (that.incidentErrorMessage == null) {
            return this.jobKey != null ? this.jobKey.equals(that.jobKey) : that.jobKey == null;
         }

         return false;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.jobType != null ? this.jobType.hashCode() : 0;
      result = 31 * result + (this.jobRetries != null ? this.jobRetries.hashCode() : 0);
      result = 31 * result + (this.jobWorker != null ? this.jobWorker.hashCode() : 0);
      result = 31 * result + (this.jobDeadline != null ? this.jobDeadline.hashCode() : 0);
      result = 31 * result + (this.jobCustomHeaders != null ? this.jobCustomHeaders.hashCode() : 0);
      result = 31 * result + (this.incidentErrorType != null ? this.incidentErrorType.hashCode() : 0);
      result = 31 * result + (this.incidentErrorMessage != null ? this.incidentErrorMessage.hashCode() : 0);
      result = 31 * result + (this.jobKey != null ? this.jobKey.hashCode() : 0);
      return result;
   }
}
