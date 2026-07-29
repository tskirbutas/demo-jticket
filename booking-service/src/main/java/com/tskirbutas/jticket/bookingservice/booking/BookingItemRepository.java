package com.tskirbutas.jticket.bookingservice.booking;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    //TODO: Added to avoid hybernateLazyInitializer in responses. Investigate
    @Query("""
    SELECT bi
    FROM BookingItem bi
    JOIN FETCH bi.booking
    JOIN FETCH bi.ticket
    WHERE bi.booking.id = :bookingId
    """)
    List<BookingItem> findByBookingId(Long bookingId);
}
