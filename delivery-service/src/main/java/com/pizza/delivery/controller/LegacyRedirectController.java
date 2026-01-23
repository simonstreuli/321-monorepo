package com.pizza.delivery.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/deliveries")
@Hidden // Hide from OpenAPI documentation
public class LegacyRedirectController {

    private static final String V1_BASE_PATH = "/api/v1/deliveries";

    @GetMapping("/{orderId}")
    public ResponseEntity<Void> redirectGetDeliveryStatus(@PathVariable String orderId) {
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(V1_BASE_PATH + "/" + orderId))
                .build();
    }

    @GetMapping
    public ResponseEntity<Void> redirectGetAllDeliveries() {
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(V1_BASE_PATH))
                .build();
    }

    @GetMapping("/health")
    public ResponseEntity<Void> redirectHealth() {
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(V1_BASE_PATH + "/health"))
                .build();
    }
}
