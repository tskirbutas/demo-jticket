package com.tskirbutas.jticket.emailservice.email;

public interface EmailSender {
    void sendEmail(String address, String body);
}
