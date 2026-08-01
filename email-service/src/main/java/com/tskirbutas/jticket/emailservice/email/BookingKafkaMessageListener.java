package com.tskirbutas.jticket.emailservice.email;

import com.tskirbutas.jticket.core.messaging.BookingPaymentSucceededMessage;
import com.tskirbutas.jticket.core.messaging.kafka.KafkaConstants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class BookingKafkaMessageListener {

    static final String GROUP_ID = "email-service";

    final EmailService emailService;

    BookingKafkaMessageListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = KafkaConstants.TOPIC_BOOKING_PAYMENT_SUCCEEDED, groupId = GROUP_ID)
    void handleBookingPaymentSucceeded(BookingPaymentSucceededMessage message) {
        emailService.handlePaymentSucceeded(message.bookingId(), message.paymentId(), message.email());
    }
}