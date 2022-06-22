/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.entities.Query
 *  io.camunda.operate.webapp.api.v1.entities.Results
 *  javax.validation.Valid
 *  org.springframework.http.HttpStatus
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.ResponseStatus
 */
package io.camunda.operate.webapp.api.v1.rest;

import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface SearchController<T> {
    public static final String SEARCH = "/search";
    public static final String BY_KEY = "/{key}";

    @ResponseStatus(value=HttpStatus.OK)
    @PostMapping(value={"/search"}, consumes={"application/json"}, produces={"application/json"})
    public Results<T> search(Query<T> var1);

    @ResponseStatus(value=HttpStatus.OK)
    @GetMapping(value={"/{key}"}, produces={"application/json"})
    public T byKey(@Valid @PathVariable Long var1);
}
