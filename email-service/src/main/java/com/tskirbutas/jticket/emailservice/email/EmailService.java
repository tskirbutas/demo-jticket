package com.tskirbutas.jticket.emailservice.email;

import org.springframework.stereotype.Service;

/**
 * TODO: does not check if already sent -- needs persistence, another db
 */
@Service
public class EmailService {

    final EmailSender emailSender;

    EmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    void handlePaymentSucceeded(long bookingId, long paymentId, String email) {
        emailSender.sendEmail(email,
                String.format("Booking %s confirmed\nPayment ref: %s", bookingId, paymentId));
    }

    void handlePaymentFailed(long bookingId, long paymentId, String email, String failureReason) {
        emailSender.sendEmail(email,
                String.format("Booking %s payment failed\nPayment ref: %s\nFailure reason: %s", bookingId, paymentId, failureReason));
    }
}
