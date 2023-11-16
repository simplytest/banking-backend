package com.simplytest.server.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.apiData.CustomerData.CustomerType;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCustomer.CustomerValidator.class)
public @interface ValidCustomer
{
    String message() default "Bad Customer";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class CustomerValidator
            implements ConstraintValidator<ValidCustomer, CustomerData>
    {
        @Override
        public void initialize(ValidCustomer constraintAnnotation)
        {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(CustomerData value,
                ConstraintValidatorContext context)
        {
            if (value.type == CustomerType.Business)
            {
                return value.ustNumber != null && value.ustNumber.length() > 0
                        && value.companyName != null
                        && value.companyName.length() > 0;
            }

            return value.birthDay != null;
        }
    }
}
