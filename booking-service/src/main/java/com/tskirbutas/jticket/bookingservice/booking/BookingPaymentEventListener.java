package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.bookingservice.payment.PaymentCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class BookingPaymentEventListener {

    final BookingService bookingService;

    BookingPaymentEventListener(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @EventListener
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        bookingService.paymentForBookingProcessed(event.bookingId(), event.success(), event.failureReason(), event.paymentId());
    }
}