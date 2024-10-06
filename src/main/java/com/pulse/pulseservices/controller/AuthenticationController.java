package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.model.auth.AuthenticationRequest;
import com.pulse.pulseservices.model.auth.AuthenticationResponse;
import com.pulse.pulseservices.model.auth.IdAndToken;
import com.pulse.pulseservices.model.auth.RegisterRequest;
import com.pulse.pulseservices.model.auth.ResetPasswordRequest;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.AuthenticationService;
import com.pulse.pulseservices.service.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final QrService qrService;
    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Register user to the account db
        AuthenticationResponse authenticationResponse = authenticationService.register(request);

        if (Objects.isNull(authenticationResponse))
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);

        // Generate UUID and insert it in qr table
        User user = authenticationService.getAccountByEmail(request.getEmail());
        UUID uuid = qrService.generateUUID();
        qrService.saveQrToDatabaseAndAssignToUser(uuid, user);

        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<IdAndToken> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PutMapping("/password/reset")
    public ResponseEntity resetUserPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            // make sure the user exists by the email from the UI not from the accountId
            if (!accountService.isEmailInDataBase(resetPasswordRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.OK).body("Invalid credentials");
            }

            // Verify security question and answer (consider moving this logic to the service)
            String verificationStatus = accountService.verifySecurityQuestionAndAnswer(
                    resetPasswordRequest.getSecurityQuestion(),
                    resetPasswordRequest.getSecurityAnswer(),
                    resetPasswordRequest.getEmail()
            );

            if ("Security Question is incorrect".equals(verificationStatus)) {
                return ResponseEntity.status(HttpStatus.OK).body(verificationStatus);
            }

            if ("Security Answer is incorrect".equals(verificationStatus)) {
                return ResponseEntity.status(HttpStatus.OK).body(verificationStatus);
            }

            if (!"verified".equals(verificationStatus)) {
                return ResponseEntity.status(HttpStatus.OK).body(verificationStatus);
            }

            Optional<User> account = accountService.getUserByEmail(resetPasswordRequest.getEmail());
            accountService.resetPassword(account.get().getId(), resetPasswordRequest.getNewPassword());
            return ResponseEntity.ok("Successfully reset password");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}