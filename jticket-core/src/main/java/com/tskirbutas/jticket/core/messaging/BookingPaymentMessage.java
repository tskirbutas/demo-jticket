package com.tskirbutas.jticket.core.messaging;

public sealed interface BookingPaymentMessage permits BookingPaymentSucceededMessage, BookingPaymentFailedMessage {
    long paymentId();
    long bookingId();
}