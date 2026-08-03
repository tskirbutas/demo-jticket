package com.tskirbutas.jticket.bookingservice.booking;


import com.tskirbutas.jticket.bookingservice.ticket.Ticket;
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

    //Fetch tickets to avoid N+1
    @Query("""
            SELECT bi
            FROM BookingItem bi
            JOIN FETCH bi.ticket
            WHERE bi.booking.id in :bookingIds
            """)
    List<BookingItem> findWithTicketByBookingIdIn(List<Long> bookingIds);

    // We might be fighting JPA here but explicit lock on tickets is neccessary. There might be a better way
    @Query(value = """
    select t.*
    from tickets t
    join booking_items bi on bi.ticket_id = t.id
    where bi.booking_id = :bookingId
    for update
    """, nativeQuery = true)
    List<Ticket> findTicketsByBookingIdForUpdate(long bookingId);
}
