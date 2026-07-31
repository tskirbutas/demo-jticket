package com.tskirbutas.jticket.bookingservice.payment;

import jakarta.annotation.Nullable;

public record PaymentCompletedEvent(long paymentId, long bookingId, boolean success, @Nullable String failureReason) {
}
