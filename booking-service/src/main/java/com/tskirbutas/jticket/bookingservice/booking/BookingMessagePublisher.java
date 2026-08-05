package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.core.messaging.BookingPaymentMessage;

public interface BookingMessagePublisher {
    void publishBookingPaymentMessage(BookingPaymentMessage message);
}
