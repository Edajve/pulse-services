package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.model.auth.AuthenticationRequest;
import com.pulse.pulseservices.model.auth.AuthenticationResponse;
import com.pulse.pulseservices.model.auth.IdAndToken;
import com.pulse.pulseservices.model.auth.RegisterRequest;
import com.pulse.pulseservices.service.AuthenticationService;
import com.pulse.pulseservices.service.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final QrService qrService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) throws IOException {
        AuthenticationResponse register = authenticationService.register(request);
        User user = authenticationService.getAccountByEmail(request.getEmail());
        byte[] bytes = qrService.generateQr(request.getEmail());
        qrService.saveQrToDatabaseAndAssignToUser(bytes, user);
        return ResponseEntity.ok(register);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<IdAndToken> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}