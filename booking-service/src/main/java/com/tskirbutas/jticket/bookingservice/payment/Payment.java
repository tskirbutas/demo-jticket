package com.tskirbutas.jticket.bookingservice.payment;

import jakarta.persistence.*;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    long id;

    @Column(name = "booking_id")
    long bookingId;

    @Enumerated(EnumType.STRING)
    PaymentStatus status;

    public Payment() {
    }

    public Payment(long id, long bookingId, PaymentStatus status) {
        this.id = id;
        this.bookingId = bookingId;
        this.status = status;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void successful() {
        this.status = PaymentStatus.SUCCEEDED;
    }

    public void failed() {
        this.status = PaymentStatus.FAILED;
    }
}