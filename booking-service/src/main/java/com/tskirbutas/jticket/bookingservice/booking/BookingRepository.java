package com.tskirbutas.jticket.bookingservice.booking;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface BookingRepository extends JpaRepository<Booking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Booking> findByStatusAndExpiresAtBefore(
            BookingStatus status,
            Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(Long id);
}
