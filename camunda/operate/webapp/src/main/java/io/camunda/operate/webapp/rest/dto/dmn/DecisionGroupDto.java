package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.definition.DecisionDefinitionEntity;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@ApiModel(
   value = "Decision group object",
   description = "Group of decisions with the same decisionId with all versions included"
)
public class DecisionGroupDto {
   private String decisionId;
   private String name;
   private List decisions;

   public String getDecisionId() {
      return this.decisionId;
   }

   public void setDecisionId(String decisionId) {
      this.decisionId = decisionId;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List getDecisions() {
      return this.decisions;
   }

   public void setDecisions(List decisions) {
      this.decisions = decisions;
   }

   public static List createFrom(Map decisionsGrouped) {
      List groups = new ArrayList();
      decisionsGrouped.entrySet().stream().forEach((groupEntry) -> {
         DecisionGroupDto groupDto = new DecisionGroupDto();
         groupDto.setDecisionId((String)groupEntry.getKey());
         groupDto.setName(((DecisionDefinitionEntity)((List)groupEntry.getValue()).get(0)).getName());
         groupDto.setDecisions(DtoCreator.create((List)groupEntry.getValue(), DecisionDto.class));
         groups.add(groupDto);
      });
      groups.sort(new DecisionGroupComparator());
      return groups;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionGroupDto that = (DecisionGroupDto)o;
         return this.decisionId != null ? this.decisionId.equals(that.decisionId) : that.decisionId == null;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.decisionId != null ? this.decisionId.hashCode() : 0;
   }

   public static class DecisionGroupComparator implements Comparator {
      public int compare(DecisionGroupDto o1, DecisionGroupDto o2) {
         if (o1.getName() == null && o2.getName() == null) {
            return o1.getDecisionId().compareTo(o2.getDecisionId());
         } else if (o1.getName() == null) {
            return 1;
         } else if (o2.getName() == null) {
            return -1;
         } else {
            return !o1.getName().equals(o2.getName()) ? o1.getName().compareTo(o2.getName()) : o1.getDecisionId().compareTo(o2.getDecisionId());
         }
      }
   }
}
