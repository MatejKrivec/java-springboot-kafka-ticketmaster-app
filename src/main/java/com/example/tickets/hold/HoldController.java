package com.example.tickets.hold;

import com.example.tickets.event.EventNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HoldController {

    private final HoldService service;

    public HoldController(HoldService service) {
        this.service = service;
    }

    // Step 1: reserve tickets for 5 minutes (no body = 1 ticket)
    @PostMapping("/events/{id}/hold")
    public Hold hold(@PathVariable long id, @RequestBody(required = false) HoldRequest request) {
        int quantity = (request == null) ? 1 : request.quantity();
        return service.hold(id, quantity);
    }

    // Step 2: pay before the hold expires
    @PostMapping("/holds/{holdId}/pay")
    public ResponseEntity<String> pay(@PathVariable long holdId) throws InterruptedException {
        Thread.sleep(5);   // pretend to talk to a payment system
        if (service.pay(holdId)) {
            return ResponseEntity.ok("Paid!");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Hold expired or not found");
    }

    @ExceptionHandler(SoldOutException.class)
    public ResponseEntity<String> soldOut() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Sold out");
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<String> eventNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<String> invalidQuantity(InvalidQuantityException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
