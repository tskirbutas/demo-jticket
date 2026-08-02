package com.tskirbutas.jticket.bookingservice.ticket;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT t
                FROM Ticket t
                WHERE t.id in :ids
            """)
    List<Ticket> findTicketsForUpdate(@Param("ids") List<Long> ids);

    List<Ticket> findAllByStatus(TicketStatus status);

    List<Ticket> findAllByEventId(long eventId);
}
