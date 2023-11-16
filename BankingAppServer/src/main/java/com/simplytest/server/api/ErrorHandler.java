package com.simplytest.server.api;

import java.util.ArrayList;

import com.simplytest.server.utils.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.simplytest.server.Error;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class ErrorHandler
{
    @ExceptionHandler
    public ResponseEntity<?> handle(ConstraintViolationException exception)
    {
        var errors = new ArrayList<>(exception.getConstraintViolations());
        var first = errors.get(0);

        var error = new ApiError<>(Error.ConstraintViolation, first.getMessage());
        return new ResponseEntity<>(error, null, HttpStatus.BAD_REQUEST);
    }
}