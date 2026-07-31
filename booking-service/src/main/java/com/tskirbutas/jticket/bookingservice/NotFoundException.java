package com.tskirbutas.jticket.bookingservice;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
