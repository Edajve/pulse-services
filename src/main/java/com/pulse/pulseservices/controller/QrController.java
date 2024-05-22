package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.Qr;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.repositories.QrRepository;
import com.pulse.pulseservices.service.AccountService;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor

public class QrController {

    private final QrRepository qrRepository;
    private final AccountService accountService;

    @Description("Returns the QR-UUID UUID for the account")
    @GetMapping("/{accountId}")
    public ResponseEntity<Qr> getQrCodeByAccountId(@PathVariable Long accountId) {

        User account = accountService.getAccountById(accountId);

        UUID uuid = qrRepository.getUUIDById(Math.toIntExact(account.getId()));

        Qr build = new Qr().builder()
                .generatedQrID(uuid)
                .build();
        return ResponseEntity.ok(
                build
        );
    }
}
