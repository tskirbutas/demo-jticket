package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.bookingservice.BadRequestException;
import com.tskirbutas.jticket.bookingservice.NotFoundException;
import com.tskirbutas.jticket.bookingservice.payment.PaymentDetails;
import com.tskirbutas.jticket.bookingservice.payment.PaymentService;
import com.tskirbutas.jticket.bookingservice.ticket.Ticket;
import com.tskirbutas.jticket.bookingservice.ticket.TicketRepository;
import com.tskirbutas.jticket.bookingservice.ticket.TicketStatus;
import com.tskirbutas.jticket.bookingservice.ticket.TicketUnavailableException;
import com.tskirbutas.jticket.core.messaging.BookingPaymentSucceededMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
class BookingService {

    final TicketRepository ticketRepository;
    final BookingRepository bookingRepository;
    final BookingItemRepository bookingItemRepository;
    final PaymentService paymentService;
    final BookingMessagePublisher bookingMessagePublisher;

    @Value("${app.reservation-period-seconds}")
    int reservationPeriodInSeconds;

    BookingService(TicketRepository ticketRepository,
                   BookingRepository bookingRepository,
                   BookingItemRepository bookingItemRepository,
                   PaymentService paymentService,
                   BookingMessagePublisher bookingMessagePublisher) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.paymentService = paymentService;
        this.bookingMessagePublisher = bookingMessagePublisher;
    }

    List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    Booking findBookingById(long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    List<BookingItem> findByBookingId(long id) {
        return bookingItemRepository.findByBookingId(id);
    }

    @Transactional
    CreateBookingResponse createBooking(CreateBookingRequest bookingRequest) {
        if (bookingRequest.ticketIds().isEmpty()) {
            throw new BadRequestException();
        }
        if (bookingRequest.buyerEmail() == null) {
            throw new BadRequestException();
        }

        // Lock tickets
        var tickets = ticketRepository.findTicketsForUpdate(bookingRequest.ticketIds());

        //Client sent ticket ids that we cannot find
        if (tickets.size() != bookingRequest.ticketIds().size()) {
            throw new NotFoundException("One or more tickets not found");
        }

        //Check if available
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() != TicketStatus.AVAILABLE) {
                throw new TicketUnavailableException(ticket.getId());
            }
        }
        //Reserve tickets
        tickets.forEach(Ticket::reserve);

        //Create booking
        var booking = new Booking(
                bookingRequest.buyerEmail(),
                BookingStatus.IN_PROGRESS,
                Instant.now().plusSeconds(reservationPeriodInSeconds)
        );
        var savedBooking = bookingRepository.save(booking);

        //Create booking items
        var bookingItems = tickets.stream().map(
                ticket -> {
                    return new BookingItem(savedBooking, ticket);
                }
        ).toList();
        bookingItemRepository.saveAll(bookingItems);

        return new CreateBookingResponse(savedBooking.getId(), bookingItems);
    }

    @Transactional
    void expireBookings() {
        // Lock bookings for expiration check
        var expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.IN_PROGRESS, Instant.now());
        if (expiredBookings.isEmpty()) {
            return;
        }

        for (Booking booking : expiredBookings) {
            booking.expire();
        }
        var expiredBookingItems = bookingItemRepository.findWithTicketByBookingIdIn(expiredBookings.stream().map(Booking::getId).toList());
        for (BookingItem bookingItem : expiredBookingItems) {
            bookingItem.getTicket().makeAvailable();
        }
    }

    @Transactional
    PayForBookingResponse payForBooking(long bookingId, PaymentDetails paymentDetails) {
        // Lock booking row for status update
        var booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking %s not found", bookingId)));

        if (booking.status != BookingStatus.IN_PROGRESS) {
            throw new BadRequestException();
        }

        var result = paymentService.initPaymentProcessing(bookingId, paymentDetails);
        // For simplicity assumes non-null paymentId is success
        if (result.paymentId() != null) {
            booking.paymentInitialized();
        }

        return new PayForBookingResponse(result.paymentId(), result.failureReason());
    }

    @Transactional
    public void paymentForBookingProcessed(long bookingId, boolean success, String failureReason, long paymentId) {
        // Lock booking row for status update
        var booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking %s not found", bookingId)));
        if (success) {
            booking.paymentSucceeded();

            // Lock ticket row(s) for status update
            bookingItemRepository.findTicketsByBookingIdForUpdate(bookingId).forEach(Ticket::sold);

            bookingMessagePublisher.publishBookingPaymentSucceeded(new BookingPaymentSucceededMessage(booking.getId(),paymentId, booking.getBuyerEmail()));
        } else {
            //Do nothing -- allow to retry the payment.
        }
    }
}

