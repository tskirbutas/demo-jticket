package com.tskirbutas.jticket.core.messaging;

public record BookingPaymentSucceededMessage(long bookingId, long paymentId, String email) {}
