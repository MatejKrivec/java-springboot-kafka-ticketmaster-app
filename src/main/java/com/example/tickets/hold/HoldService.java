package com.example.tickets.hold;

import com.example.tickets.event.EventNotFoundException;
import com.example.tickets.event.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoldService {

    public static final int MAX_PER_PERSON = 6;

    private final EventRepository events;
    private final HoldRepository holds;

    public HoldService(EventRepository events, HoldRepository holds) {
        this.events = events;
        this.holds = holds;
    }

    @Transactional
    public Hold hold(long eventId, int quantity) {
        if (quantity < 1 || quantity > MAX_PER_PERSON) {
            throw new InvalidQuantityException(MAX_PER_PERSON);
        }
        if (events.take(eventId, quantity) == 0) {
            if (!events.exists(eventId)) {
                throw new EventNotFoundException();
            }
            throw new SoldOutException();
        }
        return holds.create(eventId, quantity);
    }

    public boolean pay(long holdId) {
        return holds.pay(holdId) == 1;
    }
}
