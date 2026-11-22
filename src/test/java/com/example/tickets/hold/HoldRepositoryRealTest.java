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

// Real repositories + test Postgres: proves the SQL works
@SpringBootTest
@Import(TestcontainersConfig.class)
class HoldRepositoryRealTest {

    @Autowired EventRepository events;
    @Autowired HoldRepository holds;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM holds");
        jdbc.update("UPDATE events SET tickets_left = 10 WHERE id = 1");
    }

    // Asking for 11 of 10 -> nothing taken
    @Test
    void asking_for_too_many_takes_nothing() {
        int rowsChanged = events.take(1L, 11);

        assertThat(rowsChanged).isEqualTo(0);
        assertThat(events.ticketsLeft(1L)).isEqualTo(10);
    }

    // Hold not paid in time -> tickets come back
    @Test
    void expired_hold_gives_tickets_back() {
        events.take(1L, 3);
        holds.create(1L, 3);
        jdbc.update("UPDATE holds SET expires_at = now() - interval '1 minute'");

        holds.releaseExpired();

        assertThat(events.ticketsLeft(1L)).isEqualTo(10);
    }
}
