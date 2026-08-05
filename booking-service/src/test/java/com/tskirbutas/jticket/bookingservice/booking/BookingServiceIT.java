package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.bookingservice.payment.*;
import com.tskirbutas.jticket.bookingservice.ticket.Ticket;
import com.tskirbutas.jticket.bookingservice.ticket.TicketRepository;
import com.tskirbutas.jticket.bookingservice.ticket.TicketStatus;
import com.tskirbutas.jticket.bookingservice.ticket.TicketUnavailableException;
import com.tskirbutas.jticket.core.messaging.BookingPaymentSucceededMessage;
import com.tskirbutas.jticket.core.messaging.kafka.KafkaConstants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
public class BookingServiceIT {

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

    @Autowired
    private KafkaContainer kafka;

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
        var requestContent = new CreateBookingRequest(List.of(t1.getId(), t2.getId()), "buyer123@demo.com");

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestContent)))
                .andExpect(status().isCreated());

        //validate bookings
        var bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);

        //validate booking items
        assertThat(bookingItemRepository.count()).isEqualTo(2);

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
        var requestContent = new CreateBookingRequest(List.of(t1.getId(), t2.getId(), t3.getId()), "buyer123@demo.com");

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestContent)))
                .andExpect(status().isConflict());

        //validate bookings
        assertThat(bookingRepository.count()).isEqualTo(0);

        //validate booking items
        assertThat(bookingItemRepository.count()).isEqualTo(0);

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
        var requestContent = new CreateBookingRequest(List.of(t1.getId(), t2.getId(), -123456L), "buyer123@demo.com");

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestContent)))
                .andExpect(status().isNotFound());

        //validate bookings
        assertThat(bookingRepository.count()).isEqualTo(0);

        //validate booking items
        assertThat(bookingItemRepository.count()).isEqualTo(0);

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
        assertThat(bookingRepository.count()).isEqualTo(0);

        //validate booking items
        assertThat(bookingItemRepository.count()).isEqualTo(0);

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
                String buyerEmail = String.format("buyer%d@demo.com",i);
                executor.submit(() -> {
                    try {
                        // all threads line up here first, so they hit createBooking at roughly the same instant
                        readyLatch.countDown();
                        startLatch.await();

                        bookingService.createBooking(new CreateBookingRequest(List.of(contestedTicketId), buyerEmail));
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
    void expireBookings_shouldWork() {
        var t1 = ticketRepository.save(new Ticket(1, "A1", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));
        var t2 = ticketRepository.save(new Ticket(1, "A2", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));
        var t3 = ticketRepository.save(new Ticket(1, "A3", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));
        var t4 = ticketRepository.save(new Ticket(1, "A4", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));
        var t5 = ticketRepository.save(new Ticket(1, "A5", BigDecimal.valueOf(44.99), TicketStatus.SOLD));
        var t6 = ticketRepository.save(new Ticket(1, "A6", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));

        var booking1 = bookingRepository.save(new Booking("buyer123@demo.com", BookingStatus.IN_PROGRESS, Instant.now().minusSeconds(60 * 15)));
        bookingItemRepository.save(new BookingItem(booking1, t1));
        bookingItemRepository.save(new BookingItem(booking1, t3));

        var booking2 = bookingRepository.save(new Booking("buyer321@demo.com", BookingStatus.IN_PROGRESS, Instant.now().plusSeconds(60 * 15)));
        bookingItemRepository.save(new BookingItem(booking2, t4));

        var booking3 = bookingRepository.save(new Booking("buyer789@demo.com", BookingStatus.PAYMENT_SUCCEEDED, Instant.now().minusSeconds(60 * 15)));
        bookingItemRepository.save(new BookingItem(booking3, t5));

        var booking4 = bookingRepository.save(new Booking("buyer789@demo.com", BookingStatus.PAYMENT_INITIALIZED, Instant.now().minusSeconds(60 * 15)));
        bookingItemRepository.save(new BookingItem(booking4, t6));


        bookingService.expireBookings();


        //validate bookings -- all bookings should be kept
        assertThat(bookingRepository.count()).isEqualTo(4);
        booking1 = bookingRepository.findById(booking1.getId()).orElseThrow();
        assertThat(booking1.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        booking2 = bookingRepository.findById(booking2.getId()).orElseThrow();
        assertThat(booking2.getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
        booking3 = bookingRepository.findById(booking3.getId()).orElseThrow();
        assertThat(booking3.getStatus()).isEqualTo(BookingStatus.PAYMENT_SUCCEEDED);
        booking4 = bookingRepository.findById(booking4.getId()).orElseThrow();
        assertThat(booking4.getStatus()).isEqualTo(BookingStatus.PAYMENT_INITIALIZED);

        //validate booking items -- all booking items should be kept
        assertThat(bookingItemRepository.count()).isEqualTo(5);

        //validate ticket statuses
        t1 = ticketRepository.findById(t1.getId()).orElseThrow();
        assertThat(t1.getStatus()).as("ticket reservation should be expired").isEqualTo(TicketStatus.AVAILABLE);
        t2 = ticketRepository.findById(t2.getId()).orElseThrow();
        assertThat(t2.getStatus()).as("ticket not booked so should be unchanged").isEqualTo(TicketStatus.AVAILABLE);
        t3 = ticketRepository.findById(t3.getId()).orElseThrow();
        assertThat(t3.getStatus()).as("ticket reservation should be expired").isEqualTo(TicketStatus.AVAILABLE);
        t4 = ticketRepository.findById(t4.getId()).orElseThrow();
        assertThat(t4.getStatus()).as("tickets that are not due should not be expired").isEqualTo(TicketStatus.RESERVED);
        t5 = ticketRepository.findById(t5.getId()).orElseThrow();
        assertThat(t5.getStatus()).as("sold tickets should stay sold").isEqualTo(TicketStatus.SOLD);
        t6 = ticketRepository.findById(t6.getId()).orElseThrow();
        assertThat(t6.getStatus()).as("while payment is being processed should not expire").isEqualTo(TicketStatus.RESERVED);
    }

    @Test
    void postBookingPay_regularFlow_shouldSellTickets() throws Exception {
        var t1 = ticketRepository.save(new Ticket(1, "A1", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));
        var t2 = ticketRepository.save(new Ticket(1, "A2", BigDecimal.valueOf(44.99), TicketStatus.RESERVED));
        var t3 = ticketRepository.save(new Ticket(1, "A3", BigDecimal.valueOf(44.99), TicketStatus.AVAILABLE));

        var buyerEmail = "buyer123@demo.com";
        var booking = bookingRepository.save(new Booking(buyerEmail, BookingStatus.IN_PROGRESS, Instant.now().plusSeconds(60 * 15)));
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

        //validate kafka message published
        try(var kafkaConsumer = createTestKafkaConsumer(BookingPaymentSucceededMessage.class)) {
            kafkaConsumer.subscribe(List.of(KafkaConstants.TOPIC_BOOKING_PAYMENT_SUCCEEDED));

            var record = KafkaTestUtils.getSingleRecord(kafkaConsumer, KafkaConstants.TOPIC_BOOKING_PAYMENT_SUCCEEDED);
            assertThat(record.value().paymentId()).isEqualTo(payment.getId());
        }
    }

    <T> Consumer<String, T> createTestKafkaConsumer(Class<T> valueDefaultType) {
         var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, valueDefaultType.getName());

        var factory = new DefaultKafkaConsumerFactory<String, T>(props);
        return factory.createConsumer();
    }
}
