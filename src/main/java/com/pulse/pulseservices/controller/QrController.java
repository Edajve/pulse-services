package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.Qr;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.repositories.QrRepository;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.QrService;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor

public class QrController {

    private final QrRepository qrRepository;
    private final AccountService accountService;
    private final QrService qrService;

    @Description("Returns the QR-UUID UUID for the account")
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getQrCodeByAccountId(@PathVariable Long accountId) {

        User account = accountService.getAccountById(accountId);

        byte[] uuidBytes = qrService.getUUIDByUserId(accountId);

        if (Objects.isNull(uuidBytes))
            return new ResponseEntity<>("There is no UUID associated with id of : " + accountId,
                    HttpStatus.NOT_FOUND);

        UUID uuid = qrService.bytesToUUID(uuidBytes);

        Qr build = new Qr().builder()
                .id(Long.valueOf(account.getId()))
                .generatedQrID(uuid)
                .build();

        build.setGeneratedQrID(uuid);
        return ResponseEntity.ok(build);
    }
}
