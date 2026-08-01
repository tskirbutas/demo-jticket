package com.tskirbutas.jticket.emailservice.email;

public interface EmailSender {
    void sendConfirmationEmailForBooking(String address, String body);
}
