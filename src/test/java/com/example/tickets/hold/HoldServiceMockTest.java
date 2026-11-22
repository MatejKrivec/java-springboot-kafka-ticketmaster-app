package com.example.tickets.hold;

import com.example.tickets.event.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// Real HoldService, fake repositories
@ExtendWith(MockitoExtension.class)
class HoldServiceMockTest {

    @Mock EventRepository events;
    @Mock HoldRepository holds;
    @InjectMocks HoldService service;

    // Tickets available -> returns the hold
    @Test
    void tickets_available_returns_hold() {
        // Arrange
        Hold fakeHold = new Hold(7, 2, OffsetDateTime.now().plusMinutes(5));
        when(events.take(1L, 2)).thenReturn(1);
        when(holds.create(1L, 2)).thenReturn(fakeHold);

        // Act
        Hold result = service.hold(1L, 2);

        // Assert
        assertThat(result).isEqualTo(fakeHold);
    }

    // Sold out -> SoldOutException
    @Test
    void sold_out_throws_exception() {
        when(events.take(1L, 2)).thenReturn(0);
        when(events.exists(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.hold(1L, 2))
                .isInstanceOf(SoldOutException.class);
    }

    // More than 6 tickets -> InvalidQuantityException
    @Test
    void too_many_tickets_throws_exception() {
        assertThatThrownBy(() -> service.hold(1L, 7))
                .isInstanceOf(InvalidQuantityException.class);
    }
}
