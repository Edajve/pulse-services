package com.pulse.pulseservices.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/health")
@RequiredArgsConstructor
public class Health {

    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Healthy");
    }
}
