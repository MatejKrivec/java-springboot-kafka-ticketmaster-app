package com.example.tickets.hold;

// JSON body of POST /events/{id}/hold, e.g. {"quantity": 3}
public record HoldRequest(int quantity) {}
