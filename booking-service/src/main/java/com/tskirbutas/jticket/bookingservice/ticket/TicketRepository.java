package com.tskirbutas.jticket.bookingservice.ticket;


import org.springframework.data.jpa.repository.JpaRepository;

interface TicketRepository extends JpaRepository<Ticket, Long> {

}
