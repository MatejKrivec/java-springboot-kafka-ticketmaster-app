package com.example.tickets.hold;

import com.example.tickets.event.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// Fake JdbcTemplate: the SQL never runs, so these tests pass even if the SQL is wrong.
// Kept only as a learning example of why repositories should be tested on a real database.
@ExtendWith(MockitoExtension.class)
class HoldRepositoryMockTest {

    @Mock JdbcTemplate jdbc;
    @InjectMocks EventRepository events;
    @InjectMocks HoldRepository holds;

    // Too many tickets -> 0 rows changed
    @Test
    void asking_for_too_many_takes_nothing() {
        when(jdbc.update(anyString(), eq(11), eq(1L), eq(11))).thenReturn(0);

        assertThat(events.take(1L, 11)).isEqualTo(0);
    }

    // Expired hold -> release returns rows changed
    @Test
    void expired_hold_gives_tickets_back() {
        when(jdbc.update(anyString())).thenReturn(1);

        assertThat(holds.releaseExpired()).isEqualTo(1);
    }
}
