package io.camunda.operate.webapp.api.v1.rest;

import io.camunda.operate.webapp.api.v1.entities.Query;
import io.camunda.operate.webapp.api.v1.entities.Results;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface SearchController {
   String SEARCH = "/search";
   String BY_KEY = "/{key}";

   @ResponseStatus(HttpStatus.OK)
   @PostMapping(
      value = {"/search"},
      consumes = {"application/json"},
      produces = {"application/json"}
   )
   Results search(Query var1);

   @ResponseStatus(HttpStatus.OK)
   @GetMapping(
      value = {"/{key}"},
      produces = {"application/json"}
   )
   Object byKey(@PathVariable @Valid Long var1);
}
