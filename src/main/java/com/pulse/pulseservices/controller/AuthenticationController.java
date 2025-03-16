package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.ResetPasswordStatus;
import com.pulse.pulseservices.model.auth.AuthenticateWithPinRequest;
import com.pulse.pulseservices.model.auth.AuthenticationRequest;
import com.pulse.pulseservices.model.auth.AuthenticationResponseForPin;
import com.pulse.pulseservices.model.auth.RegisterRequest;
import com.pulse.pulseservices.model.auth.RegisterResponse;
import com.pulse.pulseservices.model.auth.ResetPasswordRequest;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        var registeredUser = authenticationService.registerUser(request);
        return Objects.isNull(registeredUser)
                ? ResponseEntity.ok("User already exists")
                : ResponseEntity.ok(registeredUser);
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

    @GetMapping("/authMethod")
    public ResponseEntity<String> getAuthMethodByLocalHash(@RequestParam String localHash) {

        String authMethod = accountService.getAuthMethodByLocalHash(localHash);

        if (authMethod == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user found for this local hash");
        }

        return ResponseEntity.ok(authMethod);
    }

    @PostMapping("/authenticate/pin")
    public ResponseEntity<AuthenticationResponseForPin> registerWithPin(@RequestBody AuthenticateWithPinRequest request) {
        Optional<AuthenticationResponseForPin> authenticatedUserWithPin = authenticationService.authenticateUserWithPin(request);
        return authenticatedUserWithPin.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(null));
    }

    @PostMapping("/authenticate/hash/{localHash}")
    public ResponseEntity<AuthenticationResponseForPin> registerWithPin(@PathVariable String localHash) {

        Optional<AuthenticationResponseForPin> authenticatedUserWithPin = authenticationService.authenticateUserWithHash(localHash);

        return authenticatedUserWithPin.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
    }
}