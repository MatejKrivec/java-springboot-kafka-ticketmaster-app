package com.example.tickets.event;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    private Long id;

    private String name;

    @Column(name = "tickets_left")
    private int ticketsLeft;

    protected EventEntity() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getTicketsLeft() { return ticketsLeft; }
}