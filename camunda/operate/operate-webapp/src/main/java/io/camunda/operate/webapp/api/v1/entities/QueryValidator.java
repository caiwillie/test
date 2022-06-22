/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.Query$Sort
 *  io.camunda.operate.webapp.api.v1.entities.QueryValidator$CustomQueryValidator
 *  io.camunda.operate.webapp.api.v1.exceptions.ClientException
 *  io.camunda.operate.webapp.api.v1.exceptions.ValidationException
 */
package io.camunda.operate.webapp.api.v1.entities;

import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.QueryValidator;
import io.camunda.operate.webapp.api.v1.exceptions.ClientException;
import io.camunda.operate.webapp.api.v1.exceptions.ValidationException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class QueryValidator<T> {
    private List<String> fields;

    public void validate(Query<T> query, Class<T> queriedClass) throws ValidationException {
        this.validate(query, queriedClass, null);
    }

    public void validate(Query<T> query, Class<T> queriedClass, CustomQueryValidator<T> customValidator) {
        this.retrieveFieldsFor(queriedClass);
        this.validateSorting(query.getSort(), this.fields);
        this.validatePaging(query);
        if (customValidator == null) return;
        customValidator.validate(query);
    }

    private void retrieveFieldsFor(Class<T> queriedClass) {
        if (this.fields != null) return;
        this.fields = Arrays.stream(queriedClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
    }

    protected void validatePaging(Query<T> query) {
        int size = query.getSize();
        if (size <= 0) throw new ClientException("size should be greater than zero and lesser than 1_000");
        if (size > 1000) {
            throw new ClientException("size should be greater than zero and lesser than 1_000");
        }
        Object[] searchAfter = query.getSearchAfter();
        if (searchAfter != null && searchAfter.length == 0) {
            throw new ValidationException("searchAfter should have a least 1 value");
        }
        if (query.getSort() == null) return;
        int sortSize = query.getSort().size();
        if (searchAfter == null) return;
        if (searchAfter.length == sortSize + 1) return;
        throw new ValidationException(String.format("searchAfter should have a %s values", sortSize + 1));
    }

    protected void validateSorting(List<Query.Sort> sortSpecs, List<String> fields) {
        if (sortSpecs == null) return;
        if (sortSpecs.isEmpty()) {
            return;
        }
        List givenFields = CollectionUtil.withoutNulls((Collection)sortSpecs.stream().map(Query.Sort::getField).collect(Collectors.toList()));
        if (givenFields.isEmpty()) {
            throw new ValidationException("No 'field' given in sort. Example: \"sort\": [{\"field\":\"name\",\"order\": \"ASC\"}] ");
        }
        List<String> invalidSortSpecs = this.getInvalidFields(fields, givenFields);
        if (invalidSortSpecs.isEmpty()) return;
        throw new ValidationException(String.format("Sort has invalid field(s): %s", String.join((CharSequence)", ", invalidSortSpecs)));
    }

    private List<String> getInvalidFields(List<String> availableFields, List<String> givenFields) {
        return givenFields.stream().filter(field -> !availableFields.contains(field)).collect(Collectors.toList());
    }

    public static interface CustomQueryValidator<T> {
        public void validate(Query<T> var1) throws ValidationException;
    }
}
