package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.bookingservice.BadRequestException;
import com.tskirbutas.jticket.bookingservice.NotFoundException;
import com.tskirbutas.jticket.bookingservice.ticket.Ticket;
import com.tskirbutas.jticket.bookingservice.ticket.TicketUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
class BookingController {

    final BookingService bookingService;

    BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Debug mapping
    @GetMapping
    List<Booking> findAllBookings() {
        return bookingService.findAll();
    }

    // Debug mapping
    @GetMapping("/{id}")
    Booking findBookingById(@PathVariable long id) {
        return bookingService.findBookingById(id);
    }

    @GetMapping("/{id}/tickets")
    List<Ticket> findTicketsById(@PathVariable long id) {
        return bookingService.findTicketsByBookingId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreateBookingResponse createBooking(@RequestBody CreateBookingRequest bookingRequest) {
        return bookingService.createBooking(bookingRequest);
    }
    @PostMapping("/{id}/pay")
    PayForBookingResponse payForBooking(@PathVariable long id, @RequestBody PayForBookingRequest payRequest) {
        return bookingService.payForBooking(id, payRequest.paymentDetails());
    }

    @ExceptionHandler(TicketUnavailableException.class)
    public ErrorResponse handleTicketUnavailable(
            TicketUnavailableException e) {
        return new ErrorResponseException(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(BadRequestException.class)
    public ErrorResponse handleBadRequest(BadRequestException e) {
        return new ErrorResponseException(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNotFound(NotFoundException e) {
        return new ErrorResponseException(HttpStatus.NOT_FOUND, e);
    }
}
