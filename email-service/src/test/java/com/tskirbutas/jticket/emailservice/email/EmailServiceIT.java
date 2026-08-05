package com.tskirbutas.jticket.emailservice.email;


import com.tskirbutas.jticket.core.messaging.BookingPaymentFailedMessage;
import com.tskirbutas.jticket.core.messaging.BookingPaymentSucceededMessage;
import com.tskirbutas.jticket.core.messaging.kafka.KafkaConstants;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.HashMap;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ImportTestcontainers(EmailTestContainerConfiguration.class)
class EmailServiceIT {

    @MockitoSpyBean
    EmailSenderFake emailSender;

    @Autowired
    private KafkaContainer kafka;

    @Test
    void consumeBookingPaymentSucceededMessage_shouldInvokeEmailSender() {
        var message = new BookingPaymentSucceededMessage(123L, 987L, "buyer1@demo.com");
        var producer = createTestKafkaProducer();
        producer.send(KafkaConstants.TOPIC_BOOKING_PAYMENT_PROCESSED, message);

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(emailSender).sendEmail(eq(message.email()), Mockito.anyString());
        });
    }

    @Test
    void consumeBookingPaymentFailedMessage_shouldInvokeEmailSender() {
        var failureReason = "Could not transfer funds";
        var message = new BookingPaymentFailedMessage(123L, 987L, "buyer1@demo.com", failureReason);
        var producer = createTestKafkaProducer();
        producer.send(KafkaConstants.TOPIC_BOOKING_PAYMENT_PROCESSED, message);

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(emailSender).sendEmail(eq(message.email()), Mockito.contains(failureReason));
        });
    }


    <T> KafkaTemplate<String, T> createTestKafkaProducer() {
        var props = new HashMap<String, Object>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        var producer = new DefaultKafkaProducerFactory<String, T>(props);
        return new KafkaTemplate<>(producer);
    }

}

