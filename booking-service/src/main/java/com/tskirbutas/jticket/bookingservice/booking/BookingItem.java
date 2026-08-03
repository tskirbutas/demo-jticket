package com.tskirbutas.jticket.bookingservice.booking;


import com.tskirbutas.jticket.bookingservice.ticket.Ticket;
import jakarta.persistence.*;

@Entity
@Table(name = "booking_items")
class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    Booking booking;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    Ticket ticket;

    BookingItem() {
    }

    BookingItem(Booking booking, Ticket ticket) {
        this.booking = booking;
        this.ticket = ticket;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}