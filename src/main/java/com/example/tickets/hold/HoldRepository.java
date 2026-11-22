package com.example.tickets.hold;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HoldRepository {

    private final JdbcTemplate jdbc;

    public HoldRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Hold create(long eventId, int quantity) {
        return jdbc.queryForObject("""
                INSERT INTO holds (event_id, quantity, status, expires_at)
                VALUES (?, ?, 'HELD', now() + interval '5 minutes')
                RETURNING id, quantity, expires_at
                """, new DataClassRowMapper<>(Hold.class), eventId, quantity);
    }

    // 1 = paid, 0 = expired / already paid / unknown
    public int pay(long holdId) {
        return jdbc.update("""
                UPDATE holds SET status = 'PAID'
                WHERE id = ? AND status = 'HELD' AND expires_at > now()
                """, holdId);
    }

    // Marks expired holds and gives their tickets back, in one statement
    public int releaseExpired() {
        return jdbc.update("""
                WITH expired AS (
                    UPDATE holds SET status = 'EXPIRED'
                    WHERE status = 'HELD' AND expires_at < now()
                    RETURNING event_id, quantity
                )
                UPDATE events e SET tickets_left = e.tickets_left + c.n
                FROM (SELECT event_id, SUM(quantity) AS n FROM expired GROUP BY event_id) c
                WHERE e.id = c.event_id
                """);
    }
}
