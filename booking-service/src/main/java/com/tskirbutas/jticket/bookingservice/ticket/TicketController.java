package com.tskirbutas.jticket.bookingservice.ticket;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
    TODO:
    There is little real purpose to expose ticket read operations via REST.
    While write operations could be done via some admin console and would justify the endpoints,
    an admin could batch insert the tickets directly into db.
*/
@RestController
@RequestMapping("/ticket")
class TicketController {

    private TicketRepository ticketRepository;

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

    @GetMapping("/{id}")
    Ticket findTicketById(@PathVariable long id) {
        return ticketRepository.findById(id).orElse(null);
    }

}
