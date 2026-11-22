package com.example.tickets.event;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventRepository {

    private final JdbcTemplate jdbc;

    public EventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Atomic, all or nothing. Returns 1 = taken, 0 = not enough tickets (or unknown event)
    public int take(long id, int quantity) {
        return jdbc.update("""
                UPDATE events SET tickets_left = tickets_left - ?
                WHERE id = ? AND tickets_left >= ?
                """, quantity, id, quantity);
    }

    public boolean exists(long id) {
        return jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM events WHERE id = ?)", Boolean.class, id);
    }

    public Event findById(long id) {
        return jdbc.queryForObject(
                "SELECT id, name, tickets_left FROM events WHERE id = ?",
                new DataClassRowMapper<>(Event.class), id);
    }

    public List<Event> findAll() {
        return jdbc.query(
                "SELECT id, name, tickets_left FROM events ORDER BY id",
                new DataClassRowMapper<>(Event.class));
    }

    public int ticketsLeft(long id) {
        return jdbc.queryForObject(
                "SELECT tickets_left FROM events WHERE id = ?", Integer.class, id);
    }
}
