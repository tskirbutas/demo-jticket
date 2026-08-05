package com.tskirbutas.jticket.core.messaging;

public record BookingPaymentFailedMessage(long bookingId, long paymentId, String email,
                                          String failureReason) implements BookingPaymentMessage {
}
