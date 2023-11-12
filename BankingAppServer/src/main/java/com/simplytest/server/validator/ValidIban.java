package com.simplytest.server.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.iban4j.IbanUtil;

import com.simplytest.server.data.Iban;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidIban.IbanValidator.class)
public @interface ValidIban
{
    String message() default "Bad Iban";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class IbanValidator implements ConstraintValidator<ValidIban, Iban>
    {
        @Override
        public void initialize(ValidIban constraintAnnotation)
        {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(Iban value, ConstraintValidatorContext context)
        {
            try
            {
                IbanUtil.validate(value.raw());
            } catch (Exception e)
            {
                return false;
            }

            return true;
        }
    }
}