package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.bookingservice.ticket.Ticket;
import com.tskirbutas.jticket.bookingservice.ticket.TicketRepository;
import com.tskirbutas.jticket.bookingservice.ticket.TicketStatus;
import com.tskirbutas.jticket.bookingservice.ticket.TicketUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BookingService {
    //TODO: move reservation period to config
    public static final int RESERVATION_PERIOD_IN_SECONDS = 60 * 15;

    private TicketRepository ticketRepository;
    private BookingRepository bookingRepository;
    private BookingItemRepository bookingItemRepository;

    BookingService(TicketRepository ticketRepository, BookingRepository bookingRepository, BookingItemRepository bookingItemRepository) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
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
        if (bookingRequest.buyerId() == null) {
            throw new BadRequestException();
        }

        List<Ticket> tickets = ticketRepository.findTicketsForUpdate(bookingRequest.ticketIds());

        //Client sent ticket ids that we cannot find
        if (tickets.size() != bookingRequest.ticketIds().size()) {
            throw new BadRequestException(); //TODO: Could be HTTP.NOT_FOUND with some data
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
        Booking booking = new Booking();
        booking.setBuyerId(bookingRequest.buyerId());
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setExpiresAt(Instant.now().plusSeconds(RESERVATION_PERIOD_IN_SECONDS));
        Booking savedBooking = bookingRepository.save(booking);

        //Create booking items
        List<BookingItem> bookingItems = tickets.stream().map(
                ticket -> {
                    BookingItem item = new BookingItem();
                    item.setBooking(savedBooking);
                    item.setTicket(ticket);
                    return item;
                }
        ).toList();
        bookingItemRepository.saveAll(bookingItems);

        return new CreateBookingResponse(savedBooking.getId(), bookingItems);
    }

    @Transactional
    void expireBookings() {
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.IN_PROGRESS, Instant.now());
        if (expiredBookings.isEmpty()) {
            return;
        }

        for (Booking booking : expiredBookings) {
            booking.expire();
        }
        List<BookingItem> expiredBookingItems = bookingItemRepository.findWithTicketByBookingIdIn(expiredBookings.stream().map(Booking::getId).toList());
        for (BookingItem bookingItem : expiredBookingItems) {
            bookingItem.getTicket().makeAvailable();
        }
    }
}

