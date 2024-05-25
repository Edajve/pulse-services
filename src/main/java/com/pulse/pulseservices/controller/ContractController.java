package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.model.CreateOrUpdateContractRequest;
import com.pulse.pulseservices.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * TODO
     * some testing needs to be done but it looks to work the most part
     */
    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody CreateOrUpdateContractRequest request) {
        try {
            contractService.createOrUpdateContract(request);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }
}
