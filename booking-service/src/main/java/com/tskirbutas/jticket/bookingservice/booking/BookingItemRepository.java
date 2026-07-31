package com.tskirbutas.jticket.bookingservice.booking;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    //Fetch tickets to avoid N+1
    @Query("""
            SELECT bi
            FROM BookingItem bi
            JOIN FETCH bi.ticket
            WHERE bi.booking.id in :bookingIds
            """)
    List<BookingItem> findWithTicketByBookingIdIn(List<Long> bookingIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT bi
            FROM BookingItem bi
            JOIN FETCH bi.ticket
            WHERE bi.booking.id in :id
            """)
    List<BookingItem> findWithTicketByBookingIdForUpdate(long id);
}
