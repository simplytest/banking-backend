package com.simplytest.server.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.simplytest.core.Id;
import com.simplytest.server.data.AccountId;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidAccountId.AccountIdValidator.class)
public @interface ValidAccountId
{
    String message() default "Bad Account ID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class AccountIdValidator
            implements ConstraintValidator<ValidAccountId, AccountId>
    {
        @Override
        public void initialize(ValidAccountId constraintAnnotation)
        {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(AccountId value, ConstraintValidatorContext context)
        {
            return Id.from(value.raw()).successful();
        }
    }
}
