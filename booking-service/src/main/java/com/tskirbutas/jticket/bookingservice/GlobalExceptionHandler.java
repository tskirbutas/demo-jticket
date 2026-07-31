package com.tskirbutas.jticket.bookingservice;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleUnexpectedException(Exception e) {
        //TODO: log
        System.out.println("GlobalExceptionHandler:");
        System.out.println(e);
        return new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException("Unexpected error occurred."));
    }
}