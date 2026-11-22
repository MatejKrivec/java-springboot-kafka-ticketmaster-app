package com.example.tickets;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

// Run while the app is running: 2,000 buyers try to hold a ticket at the same time
public class BuyerSimulator {

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:8080/events/1/hold"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        AtomicInteger bought = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 2000; i++) {
                executor.submit(() -> {
                    int status = client.send(request,
                            HttpResponse.BodyHandlers.ofString()).statusCode();
                    if (status == 200) bought.incrementAndGet();
                    return null;
                });
            }
        }

        System.out.println("Buyers who got a ticket: " + bought.get());
    }
}
