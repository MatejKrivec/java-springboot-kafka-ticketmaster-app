package com.example.tickets.hold;

import com.example.tickets.TestcontainersConfig;
import com.example.tickets.event.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Real HoldService + real repositories + test Postgres
@SpringBootTest
@Import(TestcontainersConfig.class)
class HoldServiceRealTest {

    @Autowired HoldService service;
    @Autowired EventRepository events;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM holds");
        jdbc.update("UPDATE events SET tickets_left = 10 WHERE id = 1");
    }

    // Tickets available -> returns the hold, tickets go down
    @Test
    void tickets_available_returns_hold() {
        Hold result = service.hold(1L, 2);

        assertThat(result.quantity()).isEqualTo(2);
        assertThat(events.ticketsLeft(1L)).isEqualTo(8);
    }

    // Only 1 ticket left, asking for 2 -> SoldOutException
    @Test
    void sold_out_throws_exception() {
        jdbc.update("UPDATE events SET tickets_left = 1 WHERE id = 1");

        assertThatThrownBy(() -> service.hold(1L, 2))
                .isInstanceOf(SoldOutException.class);
    }

    // More than 6 tickets -> InvalidQuantityException, nothing taken
    @Test
    void too_many_tickets_throws_exception() {
        assertThatThrownBy(() -> service.hold(1L, 7))
                .isInstanceOf(InvalidQuantityException.class);

        assertThat(events.ticketsLeft(1L)).isEqualTo(10);
    }
}
