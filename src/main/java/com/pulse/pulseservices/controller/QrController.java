package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.Qr;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.model.Token;
import com.pulse.pulseservices.model.TokenValidationResponse;
import com.pulse.pulseservices.repositories.QrRepository;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.QrService;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * Verifies the QR-UUID of a scanned user in the database and checks if the scanning user
     * (making the network call) and the scanned user have an existing contract.
     *
     * If a contract exists, performs additional checks and updates the contract.
     * If no contract exists, creates a new contract.
     *
     * @param scannedUserId the ID of the scanned user (scannie)
     * @param requestBody contains the QR-UUID of scannie
     * @return a response indicating the result of the verification and contract update/creation
     */
    @PostMapping("/authenticate/{scannedUserId}")
    public ResponseEntity<?> isUuidValid(
            @PathVariable Long scannedUserId,
            @RequestBody Token requestBody
    ) {
        boolean isValid = qrService.isUuidValid(scannedUserId, requestBody);

        if (!isValid) {
            return new ResponseEntity<>(
                    "The user-id " + scannedUserId + "'s uuid is not valid in our database"
                    , HttpStatus.CONFLICT
            );
        }

        

//        return ResponseEntity.ok(new TokenValidationResponse(isValid));

        return ResponseEntity.ok(new TokenValidationResponse());
    }
}
