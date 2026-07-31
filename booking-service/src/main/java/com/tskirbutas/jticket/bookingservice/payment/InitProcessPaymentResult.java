package com.tskirbutas.jticket.bookingservice.payment;

import jakarta.annotation.Nullable;

public record InitProcessPaymentResult(Long paymentId, @Nullable String failureReason) {
}

record InitProcessPaymentResponse(Long paymentId, @Nullable String failureReason) {
}