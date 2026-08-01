package com.tskirbutas.jticket.bookingservice.payment;

import java.time.Instant;

public record PaymentSucceededKafkaMessage(long bookingId, long paymentId, Instant occurredAt) {}
