package com.tskirbutas.jticket.bookingservice.payment;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class PaymentKafkaEventPublisher {

    public static final String TOPIC = "payment.succeeded";

    final KafkaTemplate<String, PaymentSucceededKafkaMessage> kafkaTemplate;

    PaymentKafkaEventPublisher(KafkaTemplate<String, PaymentSucceededKafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event.success()) {
            var message = new PaymentSucceededKafkaMessage(event.bookingId(), event.paymentId(), Instant.now());

            kafkaTemplate.send(TOPIC, String.valueOf(event.bookingId()), message);
        }
    }
}

