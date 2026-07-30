package com.tskirbutas.jticket.bookingservice.booking;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingExpirationJob {

    private final BookingService bookingService;

    BookingExpirationJob(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedDelay = 30_000)
    void expireBookings() {
        bookingService.expireBookings();
    }
}