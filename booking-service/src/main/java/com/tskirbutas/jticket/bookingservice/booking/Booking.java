package com.tskirbutas.jticket.bookingservice.booking;


import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "bookings")
class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "buyer_email")
    String buyerEmail;

    @Enumerated(EnumType.STRING)
    BookingStatus status;

    @Column(name = "expires_at")
    Instant expiresAt;

    Booking() {

    }

    Booking(String buyerEmail, BookingStatus status, Instant expiresAt) {
        this.buyerEmail = buyerEmail;
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

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
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

    void paymentInitialized() { this.status = BookingStatus.PAYMENT_INITIALIZED; }

    void paymentSucceeded() { this.status = BookingStatus.PAYMENT_SUCCEEDED; }
}