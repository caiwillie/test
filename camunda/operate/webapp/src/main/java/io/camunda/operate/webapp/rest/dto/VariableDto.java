package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.entities.VariableEntity;
import io.camunda.operate.util.CollectionUtil;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariableDto {
   private String id;
   private String name;
   private String value;
   private boolean isPreview;
   private boolean hasActiveOperation = false;
   @ApiModelProperty("True when variable is the first in current list")
   private boolean isFirst = false;
   private String[] sortValues;

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public boolean getIsPreview() {
      return this.isPreview;
   }

   public VariableDto setIsPreview(boolean preview) {
      this.isPreview = preview;
      return this;
   }

   public boolean isHasActiveOperation() {
      return this.hasActiveOperation;
   }

   public void setHasActiveOperation(boolean hasActiveOperation) {
      this.hasActiveOperation = hasActiveOperation;
   }

   public boolean getIsFirst() {
      return this.isFirst;
   }

   public VariableDto setIsFirst(boolean first) {
      this.isFirst = first;
      return this;
   }

   public String[] getSortValues() {
      return this.sortValues;
   }

   public VariableDto setSortValues(String[] sortValues) {
      this.sortValues = sortValues;
      return this;
   }

   public static VariableDto createFrom(VariableEntity variableEntity, List operations, boolean fullValue, int variableSizeThreshold) {
      if (variableEntity == null) {
         return null;
      } else {
         VariableDto variable = new VariableDto();
         variable.setId(variableEntity.getId());
         variable.setName(variableEntity.getName());
         if (fullValue) {
            if (variableEntity.getFullValue() != null) {
               variable.setValue(variableEntity.getFullValue());
            } else {
               variable.setValue(variableEntity.getValue());
            }

            variable.setIsPreview(false);
         } else {
            variable.setValue(variableEntity.getValue());
            variable.setIsPreview(variableEntity.getIsPreview());
         }

         if (CollectionUtil.isNotEmpty(operations)) {
            List activeOperations = CollectionUtil.filter(operations, (o) -> {
               return o.getState().equals(OperationState.SCHEDULED) || o.getState().equals(OperationState.LOCKED) || o.getState().equals(OperationState.SENT);
            });
            if (!activeOperations.isEmpty()) {
               variable.setHasActiveOperation(true);
               String newValue = ((OperationEntity)activeOperations.get(activeOperations.size() - 1)).getVariableValue();
               if (fullValue) {
                  variable.setValue(newValue);
               } else if (newValue.length() > variableSizeThreshold) {
                  variable.setValue(newValue.substring(0, variableSizeThreshold));
                  variable.setIsPreview(true);
               } else {
                  variable.setValue(newValue);
               }
            }
         }

         if (variableEntity.getSortValues() != null) {
            variable.setSortValues((String[])Arrays.stream(variableEntity.getSortValues()).map(String::valueOf).toArray((x$0) -> {
               return new String[x$0];
            }));
         }

         return variable;
      }
   }

   public static List createFrom(List variableEntities, Map operations, int variableSizeThreshold) {
      return (List)(variableEntities == null ? new ArrayList() : (List)variableEntities.stream().filter((item) -> {
         return item != null;
      }).map((item) -> {
         return createFrom(item, (List)operations.get(item.getName()), false, variableSizeThreshold);
      }).collect(Collectors.toList()));
   }
}
