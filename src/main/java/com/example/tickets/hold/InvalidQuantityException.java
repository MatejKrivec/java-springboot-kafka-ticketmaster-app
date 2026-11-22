package com.example.tickets.hold;

// -> 400 Bad Request
public class InvalidQuantityException extends RuntimeException {

    public InvalidQuantityException(int max) {
        super("Quantity must be between 1 and " + max);
    }
}
