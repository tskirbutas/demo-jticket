package com.tskirbutas.jticket.bookingservice.booking;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

interface BookingRepository extends JpaRepository<Booking, Long> {

    //TODO: might need to be locked when doing confirmation
    List<Booking> findByStatusAndExpiresAtBefore(
            BookingStatus status,
            Instant now);
}
