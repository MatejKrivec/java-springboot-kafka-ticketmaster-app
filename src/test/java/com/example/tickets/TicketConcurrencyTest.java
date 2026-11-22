package com.example.tickets;

import com.example.tickets.event.EventRepository;
import com.example.tickets.hold.HoldService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfig.class)
class TicketConcurrencyTest {

    @Autowired HoldService holdService;
    @Autowired EventRepository events;
    @Autowired JdbcTemplate jdbc;

    // 2,000 buyers at once, 10 tickets -> exactly 10 sold, never more
    @Test
    void two_thousand_buyers_only_ten_get_a_ticket() {
        jdbc.update("DELETE FROM holds");
        jdbc.update("UPDATE events SET tickets_left = 10 WHERE id = 1");
        AtomicInteger bought = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 2000; i++) {
                executor.submit(() -> {
                    try {
                        holdService.hold(1L, 1);
                        bought.incrementAndGet();
                    } catch (Exception e) {
                        // sold out
                    }
                });
            }
        }

        assertThat(bought.get()).isEqualTo(10);
        assertThat(events.ticketsLeft(1L)).isEqualTo(0);
    }
}
