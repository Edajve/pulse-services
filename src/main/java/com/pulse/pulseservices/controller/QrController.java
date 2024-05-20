package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.Qr;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.repositories.QrRepository;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.QrService;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor

public class QrController {

    private final QrService qrService;
    private final QrRepository qrRepository;
    private final AccountService accountService;

    @GetMapping("/generate")
    public String generateQrCode() {
        try {
            // The string would be a specially generated string for each user
            qrService.generateQr("Unique string here");
            return "QR code generated successfully!";
        } catch (IOException e) {
            return "Error generating QR code: " + e.getMessage();
        }
    }

    @Description("Returns the QR byte array for the account")
    @GetMapping("/{accountId}")
    public ResponseEntity<Qr> getQrCodeByAccountId(@PathVariable Long accountId) {

        User account = accountService.getAccountById(accountId);

        byte[] byteArray = qrRepository.getQrById(Math.toIntExact(account.getId()));

        Qr build = new Qr().builder()
                .imageBytes(byteArray)
                .build();
        return ResponseEntity.ok(
                build
        );
    }
}
