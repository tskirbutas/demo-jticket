package com.tskirbutas.jticket.bookingservice.booking;

import com.tskirbutas.jticket.bookingservice.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * The codebase would benefit from a lot more testing. Should be expanded when there's more time.
 * Note that most important paths are covered by integration tests {@link BookingServiceIT}
 */
@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    BookingItemRepository bookingItemRepository;

    @InjectMocks
    BookingService bookingService;

    @Test
    void findBookingById_bookingDoesNotExist_shouldThrowNotFound() {
        when(bookingRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.findBookingById(123L)
        );
    }

    @Test
    void findTicketsByBookingId_bookingDoesNotExist_shouldThrowNotFound() {
        when(bookingRepository.existsById(123L)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> bookingService.findTicketsByBookingId(123L)
        );

        verify(bookingItemRepository, never()).findTicketsByBookingId(anyLong());
    }
}
