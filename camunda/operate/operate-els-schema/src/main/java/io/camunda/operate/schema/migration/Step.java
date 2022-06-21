package io.camunda.operate.schema.migration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.time.OffsetDateTime;
import java.util.Comparator;

@JsonTypeInfo(
   use = Id.NAME
)
@JsonSubTypes({@Type(ProcessorStep.class)})
public interface Step {
   String INDEX_NAME = "indexName";
   String CREATED_DATE = "createdDate";
   String APPLIED = "applied";
   String APPLIED_DATE = "appliedDate";
   String VERSION = "version";
   String ORDER = "order";
   String CONTENT = "content";
   Comparator SEMANTICVERSION_COMPARATOR = new Comparator<Step>() {
      public int compare(Step s1, Step s2) {
         return SemanticVersion.fromVersion(s1.getVersion()).compareTo(SemanticVersion.fromVersion(s2.getVersion()));
      }
   };
   Comparator ORDER_COMPARATOR = new Comparator<Step>() {
      public int compare(Step s1, Step s2) {
         return s1.getOrder().compareTo(s2.getOrder());
      }
   };
   Comparator SEMANTICVERSION_ORDER_COMPARATOR = new Comparator<Step>() {
      public int compare(Step s1, Step s2) {
         int result = Step.SEMANTICVERSION_COMPARATOR.compare(s1, s2);
         if (result == 0) {
            result = Step.ORDER_COMPARATOR.compare(s1, s2);
         }

         return result;
      }
   };

   OffsetDateTime getCreatedDate();

   Step setCreatedDate(OffsetDateTime var1);

   OffsetDateTime getAppliedDate();

   Step setAppliedDate(OffsetDateTime var1);

   String getVersion();

   Integer getOrder();

   boolean isApplied();

   Step setApplied(boolean var1);

   String getIndexName();

   String getContent();

   String getDescription();
}
