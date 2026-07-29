package com.tskirbutas.jticket.bookingservice.booking;


import org.springframework.data.jpa.repository.JpaRepository;

interface BookingRepository extends JpaRepository<Booking, Long> {

}
