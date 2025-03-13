package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.ResetPasswordStatus;
import com.pulse.pulseservices.model.auth.AuthenticationRequest;
import com.pulse.pulseservices.model.auth.AuthenticationResponse;
import com.pulse.pulseservices.model.auth.RegisterResponse;
import com.pulse.pulseservices.model.auth.RegisterRequest;
import com.pulse.pulseservices.model.auth.ResetPasswordRequest;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.AuthenticationService;
import com.pulse.pulseservices.service.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            return new ResponseEntity<>("User already exists", HttpStatus.OK);

        // Generate UUID and insert it in qr table
        User user = authenticationService.getAccountByEmail(request.getEmail());
        UUID uuid = qrService.generateUUID();
        qrService.saveQrToDatabaseAndAssignToUser(uuid, user);

        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<RegisterResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PutMapping("/password/reset")
    public ResponseEntity resetUserPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            // make sure the user exists by the email from the UI not from the accountId
            if (!accountService.isEmailInDataBase(resetPasswordRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.OK).body("This email is not in the database");
            }

            // Verify security question and answer (consider moving this logic to the service)
            String verificationStatus = accountService.verifySecurityQuestionAndAnswer(
                    resetPasswordRequest.getSecurityQuestion(),
                    resetPasswordRequest.getSecurityAnswer(),
                    resetPasswordRequest.getEmail()
            );

            if (ResetPasswordStatus.INCORRECT_QUESTION.getMessage().equals(verificationStatus)) {
                return ResponseEntity.status(HttpStatus.OK).body(verificationStatus);
            }

            if (ResetPasswordStatus.INCORRECT_ANSWER.getMessage().equals(verificationStatus)) {
                return ResponseEntity.status(HttpStatus.OK).body(verificationStatus);
            }

            if (ResetPasswordStatus.VERIFIED.getMessage().equals(verificationStatus)) {
                return ResponseEntity.status(HttpStatus.OK).body(verificationStatus);
            }

            Optional<User> account = accountService.getUserByEmail(resetPasswordRequest.getEmail());
            accountService.resetPassword(account.get().getId(), resetPasswordRequest.getNewPassword());
            return ResponseEntity.ok("Successfully reset password");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/update/pin/{accountId}")
    public ResponseEntity<?> updatePin(
            @PathVariable Long accountId,
            @RequestParam boolean pinSetting,
            @RequestParam(required = false) String pinCode) {
        accountService.updatePinSetting(accountId, pinSetting);

        if (pinCode != null) {
            accountService.updatePinSettingAndPinCode(accountId, pinSetting, pinCode);
        }

        return ResponseEntity.ok().build();
    }
}