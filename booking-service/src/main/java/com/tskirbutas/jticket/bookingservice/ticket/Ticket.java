package com.tskirbutas.jticket.bookingservice.ticket;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "event_id")
    long eventId;

    String seat;

    BigDecimal price;

    @Enumerated(EnumType.STRING)
    TicketStatus status;


    public Ticket() {
    }

    public Ticket(long eventId, String seat, BigDecimal price, TicketStatus status) {
        this.eventId = eventId;
        this.seat = seat;
        this.price = price;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public void reserve() {
        this.status = TicketStatus.RESERVED;
    }

    public void makeAvailable() {
        this.status = TicketStatus.AVAILABLE;
    }

    public void sold() {
        this.status = TicketStatus.SOLD;
    }
}