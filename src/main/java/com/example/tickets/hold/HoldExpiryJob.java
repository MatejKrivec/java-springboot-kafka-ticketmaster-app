package com.example.tickets.hold;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HoldExpiryJob {

    private final HoldRepository holds;

    public HoldExpiryJob(HoldRepository holds) {
        this.holds = holds;
    }

    // Every 30 seconds: expired holds give their tickets back
    @Scheduled(fixedRate = 30_000)
    public void release() {
        holds.releaseExpired();
    }
}
