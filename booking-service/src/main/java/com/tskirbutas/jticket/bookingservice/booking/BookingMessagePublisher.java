package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.core.messaging.BookingPaymentSucceededMessage;

public interface BookingMessagePublisher {
    void publishBookingPaymentSucceeded(BookingPaymentSucceededMessage message);
}
