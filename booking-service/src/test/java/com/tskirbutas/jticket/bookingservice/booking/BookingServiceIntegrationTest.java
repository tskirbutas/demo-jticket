package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.bookingservice.payment.*;
import com.tskirbutas.jticket.bookingservice.ticket.Ticket;
import com.tskirbutas.jticket.bookingservice.ticket.TicketRepository;
import com.tskirbutas.jticket.bookingservice.ticket.TicketStatus;
import com.tskirbutas.jticket.bookingservice.ticket.TicketUnavailableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tskirbutas.jticket.bookingservice.payment.PaymentWebhookController.WEBHOOK_FAKE_PAYMENT_PROCESSOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers(BookingTestContainerConfiguration.class)
public class BookingServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void cleanUp() {
        paymentRepository.deleteAllInBatch();
        bookingItemRepository.deleteAllInBatch();
        bookingRepository.deleteAllInBatch();
        ticketRepository.deleteAllInBatch();
    }


    @Test
    void postBooking_regularFlow_shouldCreateBooking() throws Exception {
        var t1 = ticketRepository.save(new Ticket(1, "A1", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var t2 = ticketRepository.save(new Ticket(1, "A2", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var t3 = ticketRepository.save(new Ticket(1, "A3", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var requestContent = new CreateBookingRequest(List.of(t1.getId(), t2.getId()), 123L);

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestContent)))
                .andExpect(status().isCreated());

        //validate bookings
        var bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);

        //validate booking items
        var items = bookingItemRepository.findAll();
        assertThat(items).hasSize(2);

        //validate ticket statuses
        t1 = ticketRepository.findById(t1.getId()).orElseThrow();
        assertThat(t1.getStatus()).isEqualTo(TicketStatus.RESERVED);
        t2 = ticketRepository.findById(t2.getId()).orElseThrow();
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.RESERVED);
        t3 = ticketRepository.findById(t3.getId()).orElseThrow();
        assertThat(t3.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
    }

    @Test
    void postBooking_requestContainsReserved_shouldNotCreateBooking() throws Exception {
        var t1 = ticketRepository.save(new Ticket(1, "A1", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var t2 = ticketRepository.save(new Ticket(1, "A2", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));
        var t3 = ticketRepository.save(new Ticket(1, "A3", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var requestContent = new CreateBookingRequest(List.of(t1.getId(), t2.getId(), t3.getId()), 123L);

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestContent)))
                .andExpect(status().isConflict());

        //validate bookings
        var bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(0);

        //validate booking items
        var items = bookingItemRepository.findAll();
        assertThat(items).hasSize(0);

        //validate ticket statuses
        t1 = ticketRepository.findById(t1.getId()).orElseThrow();
        assertThat(t1.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
        t2 = ticketRepository.findById(t2.getId()).orElseThrow();
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.RESERVED);
        t3 = ticketRepository.findById(t3.getId()).orElseThrow();
        assertThat(t3.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
    }

    @Test
    void postBooking_requestContainsNonExistentIds_shouldNotCreateBooking() throws Exception {
        var t1 = ticketRepository.save(new Ticket(1, "A1", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var t2 = ticketRepository.save(new Ticket(1, "A2", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var t3 = ticketRepository.save(new Ticket(1, "A3", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var requestContent = new CreateBookingRequest(List.of(t1.getId(), t2.getId(), -123456L), 123L);

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestContent)))
                .andExpect(status().isNotFound());

        //validate bookings
        var bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(0);

        //validate booking items
        var items = bookingItemRepository.findAll();
        assertThat(items).hasSize(0);

        //validate ticket statuses
        t1 = ticketRepository.findById(t1.getId()).orElseThrow();
        assertThat(t1.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
        t2 = ticketRepository.findById(t2.getId()).orElseThrow();
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
        t3 = ticketRepository.findById(t3.getId()).orElseThrow();
        assertThat(t3.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
    }

    @Test
    void postBooking_requestContainsNoBuyerId_shouldNotCreateBooking() throws Exception {
        var t1 = ticketRepository.save(new Ticket(1, "A1", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var t2 = ticketRepository.save(new Ticket(1, "A2", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var t3 = ticketRepository.save(new Ticket(1, "A3", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var requestContent = new CreateBookingRequest(List.of(t1.getId(), t2.getId()), null);

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestContent)))
                .andExpect(status().isBadRequest());

        //validate bookings
        var bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(0);

        //validate booking items
        var items = bookingItemRepository.findAll();
        assertThat(items).hasSize(0);

        //validate ticket statuses
        t1 = ticketRepository.findById(t1.getId()).orElseThrow();
        assertThat(t1.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
        t2 = ticketRepository.findById(t2.getId()).orElseThrow();
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
        t3 = ticketRepository.findById(t3.getId()).orElseThrow();
        assertThat(t3.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
    }

    @Test
    void createBooking_concurrentAttempts_onlyOneShouldSucceed() throws InterruptedException {
        var ticket = ticketRepository.save(new Ticket(1, "A1", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var contestedTicketId = ticket.getId();

        var numberOfAttempts = 20;
        var successCount = new AtomicInteger();
        var failureCount = new AtomicInteger();
        var unexpectedFailureCount = new AtomicInteger();
        var readyLatch = new CountDownLatch(numberOfAttempts);
        var startLatch = new CountDownLatch(1);
        var doneLatch = new CountDownLatch(numberOfAttempts);

        try(ExecutorService executor = Executors.newFixedThreadPool(numberOfAttempts)) {
            for (int i = 0; i < numberOfAttempts; i++) {
                long userId = i;
                executor.submit(() -> {
                    try {
                        // all threads line up here first, so they hit createBooking at roughly the same instant
                        readyLatch.countDown();
                        startLatch.await();

                        bookingService.createBooking(new CreateBookingRequest(List.of(contestedTicketId), userId));
                        successCount.incrementAndGet();
                    } catch (TicketUnavailableException e) {
                        failureCount.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                        unexpectedFailureCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            readyLatch.await();           // wait until all threads are queued up
            startLatch.countDown();       // release them all at once
            doneLatch.await(5, TimeUnit.SECONDS);
            executor.shutdown();
        }
        assertEquals(1, successCount.get());
        assertEquals(numberOfAttempts - 1, failureCount.get());
        assertEquals(0, unexpectedFailureCount.get());

        var finalState = ticketRepository.findById(contestedTicketId).orElseThrow();
        assertEquals(TicketStatus.RESERVED, finalState.getStatus());

        var bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
    }

    @Test
    void postBookingPay_regularFlow_shouldSellTickets() throws Exception {
        var t1 = ticketRepository.save(new Ticket(1, "A1", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));
        var t2 = ticketRepository.save(new Ticket(1, "A2", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));
        var t3 = ticketRepository.save(new Ticket(1, "A3", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));

        var buyerId = 123L;
        var booking = bookingRepository.save(new Booking(buyerId, BookingStatus.IN_PROGRESS, Instant.now().plusSeconds(60 * 15)));
        bookingItemRepository.save(new BookingItem(booking, t1));
        bookingItemRepository.save(new BookingItem(booking, t2));

        var bookingId = booking.getId();
        var requestContent = new PayForBookingRequest(new PaymentDetails());

        mockMvc.perform(post(String.format("/booking/%s/pay", bookingId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestContent)))
                .andExpect(status().isOk());

        //validate bookings
        var bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.PAYMENT_INITIALIZED);

        //validate payments
        var payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        var payment = payments.get(0);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);

        var webhookRequest = new PaymentProcessingCompletedRequest(payment.getId(), true, null);
        mockMvc.perform(post(WEBHOOK_FAKE_PAYMENT_PROCESSOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookRequest)))
                .andExpect(status().isOk());

        //validate payments
        payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);

        //validate ticket statuses
        t1 = ticketRepository.findById(t1.getId()).orElseThrow();
        assertThat(t1.getStatus()).isEqualTo(TicketStatus.SOLD);
        t2 = ticketRepository.findById(t2.getId()).orElseThrow();
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.SOLD);
        t3 = ticketRepository.findById(t3.getId()).orElseThrow();
        assertThat(t3.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
    }

}
