package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.bookingservice.payment.PaymentDetails;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * Collection of records to be used data transport
 */

record CreateBookingRequest(List<Long> ticketIds, String buyerEmail) {
};

record CreateBookingResponse(long bookingId, List<BookingItem> bookingItems) {
};

record PayForBookingRequest(PaymentDetails paymentDetails) {
}

record PayForBookingResponse(Long paymentId, @Nullable String failureReason) {
}
