package io.camunda.operate.util.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
   validatedBy = {ValidLongIdValidator.class}
)
@Documented
public @interface ValidLongId {
   String message() default "Specified ID is not valid";

   Class[] groups() default {};

   Class[] payload() default {};
}
