package com.tskirbutas.jticket.bookingservice.ticket;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "TICKETS")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "event_id")
    long eventId;

    String seat;

    BigDecimal price;

    String status;

    //TODO: reserved could be handled in booking
    @Column(name = "reserved_until")
    Instant reservedUntil;


    Ticket() {
    }

    Ticket(long id, long eventId, String seat, BigDecimal price, String status, Instant reservedUntil) {
        this.id = id;
        this.eventId = eventId;
        this.seat = seat;
        this.price = price;
        this.status = status;
        this.reservedUntil = reservedUntil;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(Instant reservedUntil) {
        this.reservedUntil = reservedUntil;
    }
}