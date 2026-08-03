package com.tskirbutas.jticket.eventservice;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
class EventController {

    final EventRepository eventRepository;

    EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/{id}")
    Event findEventById(@PathVariable long id) {
        return eventRepository.findById(id).orElse(null);
    }

    @GetMapping
    List<Event> findAllEvents() {
        return eventRepository.findAll();
    }
}
