package io.camunda.operate.webapp.api.v1.entities;

import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.webapp.api.v1.exceptions.ClientException;
import io.camunda.operate.webapp.api.v1.exceptions.ValidationException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class QueryValidator {
   private List fields;

   public void validate(Query query, Class queriedClass) throws ValidationException {
      this.validate(query, queriedClass, (CustomQueryValidator)null);
   }

   public void validate(Query query, Class queriedClass, CustomQueryValidator customValidator) {
      this.retrieveFieldsFor(queriedClass);
      this.validateSorting(query.getSort(), this.fields);
      this.validatePaging(query);
      if (customValidator != null) {
         customValidator.validate(query);
      }

   }

   private void retrieveFieldsFor(Class queriedClass) {
      if (this.fields == null) {
         this.fields = (List)Arrays.stream(queriedClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
      }

   }

   protected void validatePaging(Query query) {
      int size = query.getSize();
      if (size > 0 && size <= 1000) {
         Object[] searchAfter = query.getSearchAfter();
         if (searchAfter != null && searchAfter.length == 0) {
            throw new ValidationException("searchAfter should have a least 1 value");
         } else {
            if (query.getSort() != null) {
               int sortSize = query.getSort().size();
               if (searchAfter != null && searchAfter.length != sortSize + 1) {
                  throw new ValidationException(String.format("searchAfter should have a %s values", sortSize + 1));
               }
            }

         }
      } else {
         throw new ClientException("size should be greater than zero and lesser than 1_000");
      }
   }

   protected void validateSorting(List<Query.Sort> sortSpecs, List fields) {
      if (sortSpecs != null && !sortSpecs.isEmpty()) {
         List givenFields = CollectionUtil.withoutNulls((Collection)sortSpecs.stream().map(Query.Sort::getField).collect(Collectors.toList()));
         if (givenFields.isEmpty()) {
            throw new ValidationException("No 'field' given in sort. Example: \"sort\": [{\"field\":\"name\",\"order\": \"ASC\"}] ");
         } else {
            List invalidSortSpecs = this.getInvalidFields(fields, givenFields);
            if (!invalidSortSpecs.isEmpty()) {
               throw new ValidationException(String.format("Sort has invalid field(s): %s", String.join(", ", invalidSortSpecs)));
            }
         }
      }
   }

   private List getInvalidFields(List availableFields, List givenFields) {
      return (List)givenFields.stream().filter((field) -> {
         return !availableFields.contains(field);
      }).collect(Collectors.toList());
   }

   public interface CustomQueryValidator {
      void validate(Query var1) throws ValidationException;
   }
}
