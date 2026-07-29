package com.tskirbutas.jticket.bookingservice.booking;


import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "BOOKINGS")
class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "buyer_id")
    long buyerId;

    //TODO: enum
    String status;

    Booking() {

    }

    public Booking(long id, long buyerId, String status) {
        this.id = id;
        this.buyerId = buyerId;
        this.status = status;
    }

    public Booking(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(long buyerId) {
        this.buyerId = buyerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

record CreateBookingRequest(List<Long> ticketIds, long buyerId) {
};

record ConfirmBookingRequest(long bookingId, Object paymentDetails) {
};