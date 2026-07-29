package com.tskirbutas.jticket.bookingservice.booking;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/booking")
class BookingController {

    private BookingRepository bookingRepository;
    private BookingItemRepository bookingItemRepository;

    BookingController(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
    }

    @GetMapping
    List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/{id}")
    Booking findBookingById(@PathVariable long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @GetMapping("/{id}/items")
    List<BookingItem> findBookingItemsById(@PathVariable long id) {
        return bookingItemRepository.findByBookingId(id);
    }
}
