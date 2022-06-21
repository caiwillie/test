package io.camunda.operate.util.rest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidLongIdValidator implements ConstraintValidator<ValidLongId, String> {

   @Override
   public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {
      try {
         return Long.parseLong(input) >= 0L;
      } catch (NumberFormatException var4) {
         return false;
      }
   }


}
