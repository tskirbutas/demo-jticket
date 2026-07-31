package com.tskirbutas.jticket.bookingservice;

public class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super();
    }
    public BadRequestException(String message) {
        super(message);
    }
}
