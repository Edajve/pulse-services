package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.service.QrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/qr")
public class QrController {

    @Autowired
    private QrService qrService;

    @GetMapping("/generate")
    public String generateQrCode() {
        try {
            // The string would be a specially generated string for each user
            qrService.doBasicDemo("Unique string here");
            return "QR code generated successfully!";
        } catch (IOException e) {
            return "Error generating QR code: " + e.getMessage();
        }
    }
}
