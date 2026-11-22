package com.example.tickets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Simple health check endpoint
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from tickets";
    }

}
