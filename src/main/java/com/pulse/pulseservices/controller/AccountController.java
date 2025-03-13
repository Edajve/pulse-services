package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.model.AccountStats;
import com.pulse.pulseservices.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}")
    public ResponseEntity<User> getAccountById(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("stats/{accountId}")
    public ResponseEntity<AccountStats> getAccountStatsById(@PathVariable Long accountId) {
        // make sure the user exists
        User account = accountService.getAccountById(accountId);
        AccountStats accountStats = accountService.getStats(Long.valueOf(account.getId()));
        return ResponseEntity.ok(accountStats);
    }

    @GetMapping("/authMethod")
    public ResponseEntity<String> getAuthMethodByLocalHash(@RequestParam String localHash) {
        System.out.println("Received UUID: " + localHash);

        String authMethod = accountService.getAuthMethodByLocalHash(localHash);

        if (authMethod == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user found for this local hash");
        }

        return ResponseEntity.ok(authMethod);
    }
}
