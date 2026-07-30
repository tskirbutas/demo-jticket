package com.tskirbutas.jticket.bookingservice.booking;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

enum BookingStatus {
    IN_PROGRESS,
    CONFIRMED,
    EXPIRED,
    CANCELLED
}

@Entity
@Table(name = "bookings")
class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "buyer_id")
    long buyerId;

    @Enumerated(EnumType.STRING)
    BookingStatus status;

    @Column(name = "expires_at")
    Instant expiresAt;

    Booking() {

    }

    public Booking(long buyerId, BookingStatus status, Instant expiresAt) {
        this.buyerId = buyerId;
        this.status = status;
        this.expiresAt = expiresAt;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    void expire() {
        this.status = BookingStatus.EXPIRED;
    }
}

record CreateBookingRequest(List<Long> ticketIds, Long buyerId) {
};

record CreateBookingResponse(long bookingId, List<BookingItem> bookingItems) {
};