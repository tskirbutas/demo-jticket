package com.tskirbutas.jticket.bookingservice.ticket;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
    Besides #findAllByEventId, there is little real purpose to expose ticket read operations via REST.
    While write operations could be done via some admin console and would justify the endpoints,
    an admin could batch insert the tickets directly into db.
*/
@RestController
@RequestMapping("/ticket")
class TicketController {

    final TicketRepository ticketRepository;

    TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    List<Ticket> findAllTickets() {
        return ticketRepository.findAll();
    }

    @GetMapping(params = "status")
    List<Ticket> findAllByStatus(@RequestParam("status") TicketStatus status) {
        return ticketRepository.findAllByStatus(status);
    }

    @GetMapping(params = "eventId")
    List<Ticket> findAllByEventId(@RequestParam("eventId") Long eventId) {
        //NOTE returns 200 even if no event with eventId exists.
        //This is a bit trickier to synchronize in microservice architecture
        return ticketRepository.findAllByEventId(eventId);
    }

    @GetMapping("/{id}")
    Ticket findTicketById(@PathVariable long id) {
        return ticketRepository.findById(id).orElse(null);
    }

}
