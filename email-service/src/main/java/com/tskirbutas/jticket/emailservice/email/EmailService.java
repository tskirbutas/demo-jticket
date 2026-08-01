package com.tskirbutas.jticket.emailservice.email;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    final EmailSender emailSender;

    EmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    void handlePaymentSucceeded(long bookingId, long paymentId, String email) {
        //TODO: check if already sent -- needs persistence, another db
        emailSender.sendConfirmationEmailForBooking(email,
                String.format("Booking %s confirmed\nPayment ref: %s", bookingId, paymentId));
    }
}
