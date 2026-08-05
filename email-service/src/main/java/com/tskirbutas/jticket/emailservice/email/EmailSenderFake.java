package com.tskirbutas.jticket.emailservice.email;

import org.springframework.stereotype.Component;

@Component
public class EmailSenderFake implements EmailSender {
    @Override
    public void sendEmail(String address, String body) {
        System.out.printf("EmailSenderFake --- Sending email to %s\n%s%n", address, body);
    }
}
