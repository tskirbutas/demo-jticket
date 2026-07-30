package com.tskirbutas.jticket.bookingservice.ticket;

public class TicketUnavailableException extends RuntimeException {
    public TicketUnavailableException(long id) {
        super(String.format("Ticket with id %d is not available", id));
    }
}
