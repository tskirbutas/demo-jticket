package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.core.messaging.BookingPaymentMessage;
import com.tskirbutas.jticket.core.messaging.kafka.KafkaConstants;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingKafkaMessagePublisher implements BookingMessagePublisher {

    final KafkaTemplate<String, BookingPaymentMessage> kafkaTemplate;

    BookingKafkaMessagePublisher(KafkaTemplate<String, BookingPaymentMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishBookingPaymentMessage(BookingPaymentMessage message) {
        kafkaTemplate.send(KafkaConstants.TOPIC_BOOKING_PAYMENT_PROCESSED, String.valueOf(message.bookingId()), message);
    }
}

