package com.example.tickets.hold;

import java.time.OffsetDateTime;

public record Hold(long id, int quantity, OffsetDateTime expiresAt) {}
