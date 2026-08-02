package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.core.messaging.BookingPaymentSucceededMessage;
import com.tskirbutas.jticket.core.messaging.kafka.KafkaConstants;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingKafkaMessagePublisher implements BookingMessagePublisher {

    final KafkaTemplate<String, BookingPaymentSucceededMessage> kafkaTemplate;

    BookingKafkaMessagePublisher(KafkaTemplate<String, BookingPaymentSucceededMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishBookingPaymentSucceeded(BookingPaymentSucceededMessage message) {
        kafkaTemplate.send(KafkaConstants.TOPIC_BOOKING_PAYMENT_SUCCEEDED, String.valueOf(message.bookingId()), message);
    }
}

