package com.example.tickets.event;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public Event getEvent(long id) {
        if (!repository.exists(id)) {
            throw new EventNotFoundException();
        }
        return repository.findById(id);
    }

    public List<Event> getAllEvents() {
        return repository.findAll();
    }
}
