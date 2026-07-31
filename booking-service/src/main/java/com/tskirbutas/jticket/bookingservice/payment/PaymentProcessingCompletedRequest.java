package com.tskirbutas.jticket.bookingservice.payment;

import jakarta.annotation.Nullable;

public record PaymentProcessingCompletedRequest(Long paymentId, Boolean success, @Nullable String failureReason) {
}
